package ratelimiter;

public final class RateLimiterFactory {

    //private constructor to prevent unnecessary factory instances
    private RateLimiterFactory(){}

    public static RateLimiter create(RateLimitConfig config){
        if(config instanceof FixedWindowConfig fixedWindowConfig){
            return new FixedWindowRateLimiter(fixedWindowConfig);
        }
        if(config instanceof TokenBucketConfig tokenBucketConfig){
            return new TokenBucketRateLimiter(tokenBucketConfig);
        }

        throw new IllegalArgumentException(
                "Unsupported rate limit configuration"
        );
    }
}
