package ratelimiterperendpoint;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TokenBucketRateLimiter implements Limiter{
    private final int capacity; // max tokens in bucket
    private final int refillRate; //no. of tokens refilled per second
    private final Map<String, TokenBucket> buckets; // buckets of clients

    public TokenBucketRateLimiter(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.buckets = new HashMap<>();
    }

    @Override
    public RateLimiterResult allow(String clientId) {
        /*  * Get the client bucket
            * Check lastRefillTime and refill upto capacity
            * Check if request allowed - capture remaining/ retry after
         */
        TokenBucket bucket = buckets.computeIfAbsent(clientId, v -> new TokenBucket(capacity, Instant.now()));
        Instant now = Instant.now();
        long elapsed = Duration.between(now, bucket.lastRefillTime).toMillis();
        double tokensToAdd = (elapsed * refillRate) / 1000.0;
        bucket.tokens = Math.min(capacity , bucket.tokens + tokensToAdd);
        bucket.lastRefillTime = now;

        if(bucket.tokens >= 1.0){
            //Allow request
            bucket.tokens -= 1.0;
            int remaining = (int) Math.floor(bucket.tokens);
            return new RateLimiterResult(true, remaining, null);
        }
        
        double tokensNeeded = 1.0 - bucket.tokens;
        long retryAfterMs = (long) Math.floor((tokensNeeded * 1000) / refillRate);
        return new RateLimiterResult(false, 0, retryAfterMs);
    }

    private static class TokenBucket{
        double tokens;
        Instant lastRefillTime;

        public TokenBucket(double tokens, Instant lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }

    }
}
