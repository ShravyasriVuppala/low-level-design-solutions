package taskscheduler;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public class DelayedTask implements Delayed {

    private final ScheduledTask task;
    private final long deadlineNanos;
    private final LongSupplier clock;

    public DelayedTask(ScheduledTask task, long delayNanos, LongSupplier clock){
        this.task = Objects.requireNonNull(task, "Task cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");

        if(delayNanos < 0){
            throw new IllegalArgumentException("Delay cannot be negative");
        }

        this.deadlineNanos = clock.getAsLong() + delayNanos;
    }

    public long getDeadlineNanos() {
        return deadlineNanos;
    }

    public ScheduledTask getTask(){
        return task;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remaining = this.deadlineNanos - this.clock.getAsLong();
        return unit.convert(remaining, TimeUnit.NANOSECONDS);
    }


    @Override
    public int compareTo(Delayed other) {
        DelayedTask otherTask = (DelayedTask) other;
        return Long.compare(this.deadlineNanos, otherTask.getDeadlineNanos());
    }
}
