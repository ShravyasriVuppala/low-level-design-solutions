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
RateLimiterService - owns clientID -> Strategy mapping
RateLimitConfig - Holds algorithm <-> limit settings. Limit Settings/Bucket capacity and Refill rate
RateLimiter  - Interface for rate limiting strategies. Defines allowRequest()
FixedWindowLimiter - Request count and current window start
TokenBucketLimiter - Available tokens and last refill time
UserTier - enum  

RateLimiterFactory - supporting entity rather than a domain entity. 

## Class design

## APIs
-boolean allowRequest(userId, tier)

## Design patterns used (and why)
Strategy - supports multiple rate limiting behaviors
Factory - choose rate limiter based on user tier

## Trade-offs / what I'd do with more time
