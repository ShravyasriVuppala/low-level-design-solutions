package taskscheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSchedulerDemo {

    private static final long START = System.nanoTime();

    public static void main(String[] args) throws InterruptedException {
        TaskScheduler scheduler = new TaskScheduler(2);
        scheduler.start();

        runsInDueOrder(scheduler);
        runsAtAbsoluteTime(scheduler);
        respectsMaxWorkers(scheduler);
        marksFailedTasks(scheduler);
        returnsEmptyForUnknownId(scheduler);
        cancelsPendingTasksOnShutdown(scheduler);
    }

    // Tasks submitted out of order must run in deadline order.
    private static void runsInDueOrder(TaskScheduler scheduler) {
        section("1. Tasks run in due order, not submission order");
        List<String> executionOrder = new CopyOnWriteArrayList<>();

        UUID slow = scheduler.scheduleAfter(() -> executionOrder.add("300ms"), Duration.ofMillis(300));
        UUID fast = scheduler.scheduleAfter(() -> executionOrder.add("100ms"), Duration.ofMillis(100));
        UUID mid = scheduler.scheduleAfter(() -> executionOrder.add("200ms"), Duration.ofMillis(200));

        awaitTerminal(scheduler, slow, fast, mid);
        log("Execution order: " + executionOrder + " (expected [100ms, 200ms, 300ms])");
    }

    // A task scheduled for an absolute time must not start before it.
    private static void runsAtAbsoluteTime(TaskScheduler scheduler) {
        section("2. Task scheduled at an absolute time");
        Instant dueAt = Instant.now().plusMillis(250);
        AtomicInteger earlyStarts = new AtomicInteger();

        UUID taskId = scheduler.schedule(() -> {
            if (Instant.now().isBefore(dueAt)) {
                earlyStarts.incrementAndGet();
            }
            log("Ran absolute-time task");
        }, dueAt);

        log("Status right after submit: " + scheduler.getStatus(taskId).orElseThrow());
        awaitTerminal(scheduler, taskId);
        log("Started early? " + (earlyStarts.get() > 0) + " (expected false)");
    }

    // With maxWorkers = 2, four long tasks due at once must never overlap more than 2 at a time.
    private static void respectsMaxWorkers(TaskScheduler scheduler) {
        section("3. Concurrency is capped at maxWorkers = 2");
        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxObserved = new AtomicInteger();

        UUID[] taskIds = new UUID[4];
        for (int i = 0; i < taskIds.length; i++) {
            int taskNumber = i + 1;
            taskIds[i] = scheduler.scheduleAfter(() -> {
                int now = running.incrementAndGet();
                maxObserved.accumulateAndGet(now, Math::max);
                log("Task " + taskNumber + " started (running = " + now + ")");
                sleepQuietly(300);
                running.decrementAndGet();
            }, Duration.ZERO);
        }

        awaitTerminal(scheduler, taskIds);
        log("Max concurrent tasks observed: " + maxObserved.get() + " (expected 2)");
    }

    // A task that throws must end in FAILED and must not kill a worker slot.
    private static void marksFailedTasks(TaskScheduler scheduler) {
        section("4. A throwing task is marked FAILED");
        UUID failing = scheduler.scheduleAfter(() -> {
            throw new IllegalStateException("reconciliation source unavailable");
        }, Duration.ofMillis(50));
        UUID afterFailure = scheduler.scheduleAfter(() -> log("Next task still runs"), Duration.ofMillis(100));

        awaitTerminal(scheduler, failing, afterFailure);
        log("Failing task status: " + scheduler.getStatus(failing).orElseThrow() + " (expected FAILED)");
        log("Following task status: " + scheduler.getStatus(afterFailure).orElseThrow() + " (expected COMPLETED)");
    }

    private static void returnsEmptyForUnknownId(TaskScheduler scheduler) {
        section("5. Unknown task ID");
        log("Status of random ID: " + scheduler.getStatus(UUID.randomUUID()) + " (expected Optional.empty)");
    }

    // Tasks not yet started at shutdown are cancelled; new submissions are rejected.
    private static void cancelsPendingTasksOnShutdown(TaskScheduler scheduler) throws InterruptedException {
        section("6. Shutdown");
        UUID future = scheduler.scheduleAfter(() -> log("Should never run"), Duration.ofMinutes(10));

        scheduler.shutdown();
        boolean terminated = scheduler.awaitTermination(5, TimeUnit.SECONDS);

        log("Terminated cleanly: " + terminated);
        log("Future task status: " + scheduler.getStatus(future).orElseThrow() + " (expected CANCELLED)");
        try {
            scheduler.scheduleAfter(() -> { }, Duration.ZERO);
            log("Submit after shutdown was accepted (unexpected)");
        } catch (IllegalStateException e) {
            log("Submit after shutdown rejected: " + e.getMessage());
        }
    }

    private static void awaitTerminal(TaskScheduler scheduler, UUID... taskIds) {
        for (UUID taskId : taskIds) {
            while (!isTerminal(scheduler.getStatus(taskId).orElseThrow())) {
                sleepQuietly(10);
            }
        }
    }

    private static boolean isTerminal(TaskStatus status) {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.FAILED
                || status == TaskStatus.CANCELLED;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    private static void log(String message) {
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - START);
        System.out.printf("[%5d ms] [%s] %s%n", elapsedMillis, Thread.currentThread().getName(), message);
    }
}
