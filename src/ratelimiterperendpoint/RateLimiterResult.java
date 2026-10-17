package ratelimiterperendpoint;

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
