package taskscheduler;

import java.time.Instant;
import java.util.UUID;

public class ScheduledTask {
    private final UUID id;
    private final Runnable task;
    private final Instant scheduledTime;

    private volatile TaskStatus status;

    public ScheduledTask(UUID id, Runnable task, Instant scheduledTime){
        this.id = id;
        this.task = task;
        this.scheduledTime = scheduledTime;

        this.status = TaskStatus.PENDING;
    }

    public Runnable getTask(){
        return this.task;
    }

    public TaskStatus getStatus(){
        return this.status;
    }

    //Package-private: accessible within same java package
    void setStatus(TaskStatus status){
        this.status = status;
    }
}
