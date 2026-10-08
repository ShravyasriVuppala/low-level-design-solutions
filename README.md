# Low-Level Design Practice

Mock-interview practice for SDE 2 LLD rounds, in Java 21.

## Session flow

1. **Get the prompt.** It is deliberately vague, as in a real interview.
2. **Clarify (~5 min).** Ask about scope, actors, scale and edge cases. Record the answers in `NOTES.md`.
3. **Model (~10 min).** List the entities, their relationships and a rough class diagram in `NOTES.md`.
4. **Code (~30 min).** Write the core classes, then a `<Problem>Demo.java` that runs the main flows end to end.
5. **Review.** Get interviewer-style feedback in `REVIEW.md`, then log the attempt in `PROGRESS.md`.

Aim for 45–60 minutes in total.

## Per-problem folder convention

Each problem lives in its own package under `src/`:

```
src/parkinglot/
├── NOTES.md              # copied from templates/NOTES.md
├── ParkingLotDemo.java   # runnable driver exercising the design
├── ...                   # your classes
└── REVIEW.md             # feedback after the attempt
```

Package names are lowercase with no hyphens (`parkinglot`, `bookmyshow`).

## What reviewers look for

- **Requirements:** were the right questions asked, and is the scope sensible?
- **Modelling:** clear entities, correct relationships, responsibilities in the right place.
- **SOLID:** especially single responsibility and the open/closed principle.
- **Patterns:** used where they are justified, not forced in.
- **Extensibility:** how much changes when a new requirement arrives ("now add X")?
- **Concurrency:** shared state, race conditions, thread safety where it matters.
- **Code quality:** naming, enums and interfaces, no god classes, runnable demo.

## Problem list

**Warm-up**
- [ ] Parking Lot
- [ ] Vending Machine
- [ ] Tic-Tac-Toe
- [ ] LRU Cache
- [ ] Logger

**Core SDE 2**
- [ ] Elevator System
- [ ] Library Management System
- [ ] Splitwise
- [ ] Rate Limiter
- [ ] Snake & Ladder
- [ ] ATM
- [ ] Meeting Room Scheduler

**Harder**
- [ ] BookMyShow (Movie Ticket Booking)
- [ ] Pub-Sub / Notification Service
- [ ] In-memory KV Store with TTL
- [ ] Hotel Booking
- [ ] Ride Sharing
- [ ] Food Delivery
- [ ] Chess
