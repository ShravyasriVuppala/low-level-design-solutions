package ratelimiter;
/*
    Requirements:
    1. Rate limit per endpoint. Rate limiting algorithm configured per endpoint
    2. Limit by userId
    3. Static configurations initialized at the service start
    4. Return remaining attempts (success) and retryafter(failure)

    Entities
    ratelimiter.RateLimiterService - orchestrator has limiter configs per endpoint
    ratelimiter.RateLimiter - interface allow(userId)
    ratelimiter.TokenBucketRateLimiter - maxCapacity, refillRate, map<userId, TokenBucket>
    TokenBucket - tokens, lastRefillTime
    ratelimiter.RateLimiterResult - flag, remaining, retryAfter

    apis
    ratelimiter.RateLimiterResult allow(String userId)

 */

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RateLimiterService {
    private final Map<String, RateLimiter> limiters;

    RateLimiterService(List<Map<String, Object>> configs, Map<String, RateLimiter> limiters){
        this.limiters = new HashMap<>();
        //iterate over the configs endpoint -> config
        //populate the limiters
        for(Map<String, Object> config : configs){
            String endpoint = config.get("endpoint").toString();

        }
    }
}

public interface RateLimiter {
    public RateLimiterResult allow(String userId);
}

public class TokenBucketRateLimiter implements RateLimiter {
    private final int capacity;
    private final int refillRate;
    private final Map<String, TokenBucket> buckets;

    public TokenBucketRateLimiter(int capacity, int refillRate, Map<String, TokenBucket> buckets) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.buckets = new HashMap<>();
    }

    @override
    public RateLimiterResult allow(String userId){
        //get the bucket for user
        //check if tokens to refill
        //check if available >= 1 and allow/reject
        Instant now = Instant.now();
        TokenBucket bucket = buckets.computeIfAbsent(userId, v -> new TokenBucket(capacity, now));
        long elapsed = Duration.between(now, bucket.lastRefillTime).toMillis();
        double tokensToAdd = (elapsed * refillRate) / 1000.0;
        bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
        bucket.lastRefillTime = now;
        if(bucket.tokens >= 1){
            //allow the request, calculate remaining
            bucket.tokens -= 1;
            int remaining = (int) Math.floor(bucket.tokens);
            return new RateLimiterResult(true, remaining, null);
        }
        //calculate retry after in ms
        double tokensNeeded = 1 - bucket.tokens;
        long retryAfterMs = (long) Math.ceil((tokensNeeded * 1000) / refillRate);
        return new RateLimiterResult(false, 0, retryAfterMs);
    }
    private static class TokenBucket{
        double tokens;
        Instant lastRefillTime;

        public TokenBucket(int tokens, Instant lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}

public class RateLimiterResult {
    private final boolean allowed;
    private final int remaining;
    private final Long retryAfterMs;

    public RateLimiterResult(boolean allowed, int remaining, Long retryAfterMs) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfterMs = retryAfterMs;
    }
}

public final class RateLimiterFactory {

    public static create(Map<String, Object> config){
        String algorithm = (String) config.get("algorithm");
        if(algorithm.equals("TokenBucket")){
            int capacity = (int) config.get("capacity");
            int refillRate = (int) config.get("refillRatePerSecond");
            return new TokenBucketRateLimiter(capacity, refillRate);
        }
    }
}
