package taskscheduler;

import java.time.Clock;
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
    private final ScheduledExecutorService cleanupExecutor;
    private Thread dispatcher;
    private final Semaphore semaphore;
    private volatile SchedulerState state = SchedulerState.NEW;

    private final LongSupplier nanoClock;
    private final Clock wallClock;

    private final Duration retentionPeriod = Duration.ofHours(24);

    //production constructor
    public TaskScheduler(int maxWorkers){
        this(maxWorkers, Clock.systemUTC(), System::nanoTime);
    }

    public TaskScheduler(int maxWorkers, Clock wallClock, LongSupplier nanoClock){

        if(maxWorkers <= 0){
            throw new IllegalArgumentException(
                    "maxWorkers must be greater than zero"
            );
        }

        this.taskQueue = new DelayQueue<>();
        this.taskRegistry = new ConcurrentHashMap<>();

        this.executor = Executors.newFixedThreadPool(maxWorkers);
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.semaphore = new Semaphore(maxWorkers);

        this.nanoClock = nanoClock;
        this.wallClock = wallClock;
    }

    //start the scheduler
    public synchronized void start(){
        if(state != SchedulerState.NEW){
            throw new IllegalStateException("Scheduler cannot be started");
        }
        dispatcher = new Thread(
                this::dispatchTasks,
                "task-dispatcher"
        );
        dispatcher.start();
        state = SchedulerState.RUNNING;

        //schedule cleanup executor
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupCompletedTasks,
                1,
                1,
                TimeUnit.HOURS
        );
    }

    //shutdown the scheduler
    public synchronized void shutdown(){
        if(state == SchedulerState.SHUTDOWN){
            return;
        }

        state = SchedulerState.SHUTDOWN;

        if(dispatcher != null){
            dispatcher.interrupt();
        }

        executor.shutdownNow();

        //cancel every task that hasnt been started
        for(ScheduledTask task : taskRegistry.values()){
            task.cancelIfPending();
        }

        cleanupExecutor.shutdownNow();
    }

    //separate await from shutdown so client will call based on needs
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException{
        return executor.awaitTermination(timeout, unit);
    }

    public synchronized UUID scheduleAfter(Runnable task, Duration delay){
        //check if scheduler is running
        if(state != SchedulerState.RUNNING){
            throw new IllegalStateException("Scheduler is not running");
        }

        // validates inputs, creates ScheduledTask instance and returns UUID
        Objects.requireNonNull(task, "Task cannot be null");
        Objects.requireNonNull(delay, "Delay cannot be null");

        if(delay.isNegative()){
            throw new IllegalArgumentException(
                    "Delay cannot be negative"
            );
        }
        UUID taskId = UUID.randomUUID();
        Instant scheduledTime = Instant.now(this.wallClock).plus(delay);

        ScheduledTask scheduledTask = new ScheduledTask(
                taskId,
                task,
                scheduledTime
        );

        DelayedTask delayedTask = new DelayedTask(
                scheduledTask,
                delay.toNanos(),
                nanoClock
        );
        taskQueue.put(delayedTask);
        taskRegistry.put(taskId, scheduledTask);

        return taskId;
    }

    public synchronized UUID schedule(Runnable task, Instant executionTime){

        //check if scheduler is running
        if(state != SchedulerState.RUNNING){
            throw new IllegalStateException("Scheduler is not running");
        }

        Objects.requireNonNull(executionTime, "Execution time cannot be null");

        Duration delay = Duration.between(
                Instant.now(this.wallClock),
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

        //Dont execute tasks marked as cancelled
        if(!task.markRunning()){
            return;
        }

        try{
            task.getTask().run();
            task.markCompleted();
        } catch (Exception e){
            task.markFailed();
            //Log the failure
        }
    }

    private void dispatchTasks(){
        while(!Thread.currentThread().isInterrupted()){
            boolean permitAcquired = false;
            DelayedTask delayedTask = null;
            try{
                // Wait until a worker slot is available
                semaphore.acquire();
                permitAcquired = true;

                //wait until earliest task becomes due
                delayedTask = taskQueue.take();
                // Effectively final variable for lambda
                DelayedTask currentTask = delayedTask;

                executor.execute(() -> {
                    try{
                        //noinspection ReassignedVariable
                        executeTask(currentTask.getTask());
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
                if(delayedTask != null){
                    delayedTask.getTask().cancelIfPending();
                }
                break;
            } finally {
                if(permitAcquired){
                    semaphore.release();
                }
            }
        }
    }

    private void cleanupCompletedTasks(){
        Instant cutoff = Instant.now(wallClock).minus(retentionPeriod);

        taskRegistry.entrySet().removeIf(entry -> {
            ScheduledTask task = entry.getValue();
            TaskStatus status = task.getStatus();

            boolean terminal =
                    status == TaskStatus.COMPLETED ||
                    status == TaskStatus.FAILED ||
                    status == TaskStatus.CANCELLED;

            Instant completedAt = task.getCompletedAt();

            return terminal
                    && completedAt != null
                    && !completedAt.isAfter(cutoff);
        });
    }
}
