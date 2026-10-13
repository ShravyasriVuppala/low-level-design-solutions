package ratelimiter;

public class TokenBucketRateLimiter implements RateLimiter{

    private final TokenBucketConfig config;

    private double availableTokens;
    private long lastRefillTime;

    public TokenBucketRateLimiter(TokenBucketConfig config) {
        this.config = config;
        this.availableTokens = config.capacity();
        this.lastRefillTime = System.nanoTime();
    }


    @Override
    public synchronized boolean allowRequest() {
        //check the elapsed seconds from lastrefilltime
        long now = System.nanoTime();
        //difference btwn now and lastRefill gives nanoseconds. Divide to convert to seconds
        double elapsedSeconds = (now - lastRefillTime) / 1_000_000_000;

        availableTokens = Math.min(
                config.capacity(),
                availableTokens + elapsedSeconds * config.refillTokensPerSecond()
        );
        if(availableTokens < 1.0){
            return false;
        }

        availableTokens -= 1.0;

        return true;
    }
}
