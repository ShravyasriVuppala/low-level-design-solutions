package ratelimiter;

public record TokenBucketConfig(int capacity, int refillTokensPerSecond) implements RateLimitConfig{
    public TokenBucketConfig{
        if(capacity <= 0 || refillTokensPerSecond <= 0){
            throw new IllegalArgumentException("Values must be positive");
        }
    }
}
