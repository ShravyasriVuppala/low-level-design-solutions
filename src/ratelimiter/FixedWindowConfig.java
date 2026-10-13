package ratelimiter;

public record FixedWindowConfig(int maxRequests, long windowMillis) implements RateLimitConfig{

    public FixedWindowConfig{
        if(maxRequests <= 0 || windowMillis() <= 0){
            throw new IllegalArgumentException("Values must be positive");
        }
    }
}
