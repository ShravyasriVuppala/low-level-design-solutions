package taskscheduler;

import java.time.Instant;
import java.util.UUID;

public class ScheduledTask {
    private final UUID id;
    private final Runnable task;
    private final Instant scheduledTime;

    private volatile TaskStatus status;
    private volatile Instant completedAt;

    public ScheduledTask(UUID id, Runnable task, Instant scheduledTime){
        this.id = id;
        this.task = task;
        this.scheduledTime = scheduledTime;

        this.status = TaskStatus.PENDING;
    }

    public Runnable getTask(){
        return task;
    }

    public TaskStatus getStatus(){
        return status;
    }

    public Instant getCompletedAt(){
        return completedAt;
    }

    //mark only pending tasks as running, not cancelled tasks
    synchronized boolean markRunning(){
        if(status != TaskStatus.PENDING){
            return false;
        }

        status = TaskStatus.RUNNING;
        return true;
    }

    //Cancel only pending tasks, not running tasks
    synchronized boolean cancelIfPending(){
        if(status != TaskStatus.PENDING){
            return false;
        }

        status = TaskStatus.CANCELLED;
        completedAt = Instant.now();
        return true;
    }

    //mark as completed
    synchronized void markCompleted(){
        if(status == TaskStatus.RUNNING){
            status = TaskStatus.COMPLETED;
            completedAt = Instant.now();
        }
    }

    //mark as failed
    synchronized void markFailed(){
        if(status == TaskStatus.RUNNING){
            status = TaskStatus.FAILED;
            completedAt = Instant.now();
        }
    }

    //Package-private: accessible within same java package
    void setStatus(TaskStatus status){
        this.status = status;
    }
}
