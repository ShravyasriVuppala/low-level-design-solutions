package ratelimiter;

import java.time.Duration;

public class FixedWindowRateLimiter implements RateLimiter{

    private final FixedWindowConfig config;
    private final long windowNanos;

    private long windowStart;
    private int requestCount;

    public FixedWindowRateLimiter(FixedWindowConfig config) {
        this.config = config;
        this.windowNanos = Duration.ofMillis(config.windowMillis()).toNanos();

        this.windowStart = System.nanoTime();
        this.requestCount = 0;
    }


    @Override
    public synchronized boolean allowRequest() {
        //check if request is in current window
        long now = System.nanoTime();

        if(now - windowStart >= windowNanos){
            windowStart = now;
            requestCount = 0;
            return true;
        }

        if(requestCount > config.maxRequests()){
            return false;
        }
        requestCount++;
        return true;
    }
}
