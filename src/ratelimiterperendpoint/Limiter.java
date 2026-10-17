package ratelimiterperendpoint;

public interface Limiter {
    public RateLimiterResult allow(String clientId);
}
