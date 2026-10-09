# Task Scheduler

## Clarifying questions & answers
1. **How many tasks can there be? Do clients provide the work along with the time/delay?**
   Up to a few tens of thousands of pending tasks at any time, in a single process and in memory. No persistence for now. Yes, clients provide the work. The scheduler doesn't know or care what it does; it only runs it at the right time.
2. **Can a client cancel a task?**
   Not for now. A task can't be cancelled once it's submitted.
3. **Is it okay if a task starts late?**
   Yes, a small delay is acceptable (best effort), for example when many tasks are due at once. A task must never start before its scheduled time.
4. **Does the scheduler run one task at a time, or is concurrency needed?**
   Concurrency is needed. Some tasks are long-running (minutes), and a slow task must not block other due tasks. Run tasks in parallel, up to a configurable maximum. Multiple client threads may also submit tasks at the same time.
5. **Can there be duplicate tasks?**
   Every submission is a separate task. Submitting the same work twice means it runs twice. No deduplication is needed.

## Functional requirements
1. A client can submit a task to run once at a specific time.
2. A client can submit a task to run after a given delay.
3. Tasks run when they are due, not before.
4. The client gets back some way to check the status of a submitted task.
5. Each client provides the actual work to run along with the time or delay.
6. Cancellation or deduplication is not needed.
7. Tasks should run in parallel, up to a configurable maximum number at a time.


## Non-functional requirements / constraints
1. Assume up to a few tens of thousands of pending tasks at any time, all in a single process and in memory.

## Entities & relationships

ScheduledTask - entity with scheduled time, task, Status and UUID

TaskStatus - enum with task ScheduledTask state values (PENDING, RUNNING, SUCCESS, FAILED)
TaskScheduler - Service that accepts and schedules ScheduledTask
ExecutorService (Worker threads) - Component that executes ScheduledTask
Dispatcher - waits for due tasks and submits them to executor

## Data structures 
DelayQueue - TaskScheduler gets ScheduledTask from queue only when it is ready to be executed.
             Concurrency safe. Blocks the dispatcher until there is a task ready to be polled.

ConcurrentMap - Store ScheduledTask and UUID. Used to return task status to client on get.

## Class diagram (rough)

## Design patterns used (and why)

## Trade-offs / what I'd do with more time
