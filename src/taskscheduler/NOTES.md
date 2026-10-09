# Task Scheduler

## Clarifying questions & answers
1. **How many tasks can there be? Do clients provide the work along with the time/delay?**
   Up to a few tens of thousands of pending tasks at any time, in a single process and 
   in memory. No persistence for now. Yes, clients provide the work. 
   The scheduler doesn't know or care what it does; it only runs it at the right time.
2. **Can a client cancel a task?**
   Not for now. A task can't be cancelled once it's submitted.
   3. **Is it okay if a task starts late?**
   Yes, a small delay is acceptable (best effort), 
   for example when many tasks are due at once. A task must never start before 
   its scheduled time.
   4. **Does the scheduler run one task at a time, or is concurrency needed?**
   Concurrency is needed. Some tasks are long-running (minutes), 
   and a slow task must not block other due tasks. Run tasks in parallel, 
   up to a configurable maximum. Multiple client threads may also submit tasks 
   at the same time.
   5. **Can there be duplicate tasks?**
   Every submission is a separate task. Submitting the same work twice means 
   it runs twice. No deduplication is needed.

## Functional requirements
1. Schedule a task at a specific time or after a delay.
2. Execute tasks when due, never early.
3. Support concurrent execution with configurable worker count.
4. Allow clients to check task status.
5. Support concurrent task submissions.
6. Cancellation or deduplication is not needed.

## Non-functional requirements / constraints
1. In-memory, single-process system.
2. Tens of thousands of pending tasks.
3. Thread-safe and efficient.
4. Small execution delays are acceptable.

## Entities & relationships

# Domain Entities
`ScheduledTask` - Holds task ID, Runnable, scheduled time, and status

# Enums
`TaskStatus` - Enum: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
`SchedulerState` - Enum defining scheduler lifecycle: NEW, RUNNING, SHUTDOWN

# Services/Coordinators
`DelayedTask` (Adapter) - Implements `Delayed` for scheduling
`TaskScheduler` - Coordinates scheduling and execution

# Concurrency/Infra components
`ExecutorService` - Executes tasks concurrently
`Semaphore` - Limits tasks admitted for execution
`Dispatcher Thread` - Retrieves due tasks and submits them to workers
`ScheduledExecutorService (Cleanup Executor)`- Runs periodic cleanup to remove 
                                               expired terminal tasks


## APIS
UUID schedule(Runnable task, Instant executionTime);

UUID scheduleAfter(Runnable task, Duration delay);

Optional<TaskStatus> getStatus(UUID taskId);

void start();

void shutdown();

## Data structures 
1. DelayQueue<DelayedTask>
   - Stores tasks ordered by execution deadline
   - Internally uses a priority queue.
   - Earliest deadline comes first.
   - take() blocks until a task becomes due.
   - Thread-safe.
2. ConcurrentHashMap<UUID, ScheduledTask>
   - Stores tasks for status lookup
   - Provides O(1) average task-status lookup.
   - Supports concurrent access.
3. ExecutorService
   - Fixed-size thread pool of maxWorkers.
   - Executes tasks concurrently.
4. Semaphore(maxWorkers)
   - Prevents the dispatcher from submitting more work than execution capacity.
   - Provides backpressure when all workers are busy.


## Execution Flow

              Client
                |
                v
           TaskScheduler
                |
       +--------+--------+
       |                 |
       v                 v
   DelayQueue       Task Registry      
      |            (HashMap)
      |
      v
   Dispatcher Thread
      |
      | Acquire semaphore permit
      | Wait for due task
      v
   ExecutorService
      |
      v
   Worker Thread
      |
      | PENDING → RUNNING
      | Execute Runnable
      | COMPLETED / FAILED
      | Release permit
      v
   Next Task


## Design patterns used (and why)
1. Producer–Consumer Pattern
   - Producers: Client threads submit tasks to the DelayQueue.
   - Consumer: Dispatcher thread retrieves due tasks.
   - Why: Decouples task submission from execution, allowing concurrent submissions 
     without blocking clients.
2. Command Pattern
   - Each Runnable encapsulates an operation to execute.
   - The scheduler doesn't need to know what the task actually does.
   - Why: Supports different types of tasks without changing scheduler logic.
3. Dependency Injection
   - Inject Clock and LongSupplier instead of directly accessing system time.
   - Why: Improves testability by allowing fake clocks in unit tests.
4. State Machine (Simple)
   - Tasks follow defined transitions:
     PENDING → RUNNING → COMPLETED / FAILED
   - PENDING → CANCELLED is supported during shutdown.
   - Why: Prevents invalid state transitions and ensures predictable task lifecycles.

## Trade-offs / what I'd do with more time
The main trade-off is that this is an in-memory, single-process scheduler, 
so tasks aren't durable. With more time, I'd prioritize persistence and 
recovery, configurable retries, and observability. I'd also consider more 
advanced scheduling and concurrency optimizations if the workload grows."