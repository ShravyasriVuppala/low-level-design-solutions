# Rate limiter

## Clarifying questions & answers
1. Are we implementing a single instance in-memory rate limiter? - Yes
2. Are we limiting requests per user/IP/APIKey? - User
3. What are the limits? Eg. 10 req/min - 
3. Does rate limiting vary by type of the user?- Yes
4. Do we need to implement different types of rate limiting algorithms? - yes
5. Should it be thread-safe? - Yes
6. Should we immediately reject the request if rate limited? - Yes
7. How should we return the response? Boolean or return any additional info? - boolean


## Requirements
1. Rate limiter should limit requests per user based on the configured rules.
2. Choose rate limiting algorithm at run-time
3. Should handle concurrent access
4. Should immediately reject the request if rate limited

## Out of SCope

## Entities & relationships
ratelimiter.RateLimiterService - owns clientID -> Strategy mapping
RateLimitConfig - Holds algorithm <-> limit settings. Limit Settings/Bucket capacity and Refill rate
ratelimiter.RateLimiter  - Interface for rate limiting strategies. Defines allowRequest()
FixedWindowLimiter - Request count and current window start
TokenBucketLimiter - Available tokens and last refill time
UserTier - enum  

ratelimiter.RateLimiterFactory - supporting entity rather than a domain entity. 

## Class design

## APIs
-boolean allowRequest(userId, tier)

## Design patterns used (and why)
Strategy - supports multiple rate limiting behaviors
Factory - choose rate limiter based on user tier

## Trade-offs / what I'd do with more time

## Takeaways
- Separate policy from state: share immutable tier configs, but keep counters/tokens per user.
- Strategy pattern: use a common ratelimiter.RateLimiter interface for interchangeable algorithms.
- Simple factory: create the correct limiter from its algorithm-specific configuration.
- Thread safety: ConcurrentHashMap protects initialization; per-user synchronization protects check-and-update.
- Fixed window: reset the counter after expiry; remember the boundary-burst limitation.
- Token bucket: refill lazily using elapsed time, preserve fractional tokens, and cap at capacity.
- Time measurement: use System.nanoTime() for elapsed durations.
- Retry-after: calculate time until window reset or the next full token; round upward.
- Records: concise data classes with generated constructors, accessors, equals(), hashCode(), and toString().
- Compact constructors: validate record parameters before Java automatically assigns the fields.
- Marker interfaces: group related types without requiring shared methods.
- Static factory methods: create objects without needing a factory instance.
- instanceof pattern matching: check a type and bind a typed variable without an explicit cast.
- computeIfAbsent() and lambdas: create and store a value when its key is missing; the lambda receives that key.
- synchronized: protect a sequence of state changes using the instance’s lock.
