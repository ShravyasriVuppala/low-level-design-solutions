package taskscheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.LongSupplier;

public class TaskScheduler {

    private final DelayQueue<DelayedTask> taskQueue;
    private final ConcurrentHashMap<UUID, ScheduledTask> taskRegistry;
    private final ExecutorService executor;
    private final Thread dispatcher;
    private final Semaphore semaphore;

    private final LongSupplier clock;

    public TaskScheduler(int maxWorkers){

        if(maxWorkers <= 0){
            throw new IllegalArgumentException(
                    "maxWorkers must be greater than zero"
            );
        }

        this.taskQueue = new DelayQueue<>();
        this.taskRegistry = new ConcurrentHashMap<>();

        this.executor = Executors.newFixedThreadPool(maxWorkers);
        this.semaphore = new Semaphore(maxWorkers);

        this.clock = System::nanoTime;

        this.dispatcher = new Thread(
                this::dispatchTasks,
                "task-dispatcher"
        );

        this.dispatcher.start();
    }

    public UUID scheduleAfter(Runnable task, Duration delay){
        // validates inputs, creates ScheduledTask instance and returns UUID
        Objects.requireNonNull(task, "Task cannot be null");
        Objects.requireNonNull(delay, "Delay cannot be null");

        if(delay.isNegative()){
            throw new IllegalArgumentException(
                    "Delay cannot be negative"
            );
        }
        UUID taskId = UUID.randomUUID();
        Instant scheduledTime = Instant.now().plus(delay);

        ScheduledTask scheduledTask = new ScheduledTask(
                taskId,
                task,
                scheduledTime
        );

        DelayedTask delayedTask = new DelayedTask(
                scheduledTask,
                delay.toNanos(),
                clock
        );
        taskQueue.put(delayedTask);
        taskRegistry.put(taskId, scheduledTask);

        return taskId;
    }

    public UUID schedule(Runnable task, Instant executionTime){

        Objects.requireNonNull(executionTime, "Execution time cannot be null");

        Duration delay = Duration.between(
                Instant.now(),
                executionTime
        );
        //if execution time is in the past, add delay as 0
        if(delay.isNegative()){
            delay = Duration.ZERO;
        }
        return scheduleAfter(task, delay);
    }

    public Optional<TaskStatus> getStatus(UUID taskId){

        Objects.requireNonNull(taskId, "Task ID cannot be null");

        ScheduledTask scheduledTask = taskRegistry.get(taskId);

        if(scheduledTask == null){
            return Optional.empty();
        }

        return Optional.of(scheduledTask.getStatus());
    }

    private void executeTask(ScheduledTask task){
        task.setStatus(TaskStatus.RUNNING);

        try{
            task.getTask().run();
            task.setStatus(TaskStatus.COMPLETED);
        } catch (Throwable t){
            task.setStatus(TaskStatus.FAILED);
            //Log the failure
        }
    }

    private void dispatchTasks(){
        while(!Thread.currentThread().isInterrupted()){
            boolean permitAcquired = false;
            try{
                // Wait until a worker slot is available
                semaphore.acquire();
                permitAcquired = true;

                //wait until earliest task becomes due
                DelayedTask delayedTask = taskQueue.take();

                executor.execute(() -> {
                    try{
                        executeTask(delayedTask.getTask());
                    }
                    finally {
                        semaphore.release();
                    }
                });
                // Worker is now responsible for releasing the permit
                permitAcquired = false;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RejectedExecutionException e){
                // Can happen when the executor is shutting down.
                // A rejected task has already left the DelayQueue.
                // Shutdown handling must decide whether to requeue it.
                break;
            } finally {
                if(permitAcquired){
                    semaphore.release();
                }
            }
        }
    }
}
