package ratelimiter;

import java.util.Map;

public class RateLimiterDemo {
    public static void main(String[] args) {
        Map<UserTier, RateLimitConfig> configs = Map.of(
                UserTier.FREE, new FixedWindowConfig(2, 60_000),
                UserTier.PREMIUM, new TokenBucketConfig(5, 1)
        );

        RateLimiterService service = new RateLimiterService(configs);

        System.out.println(service.allowRequest("alice", UserTier.FREE)); // true
        System.out.println(service.allowRequest("alice", UserTier.FREE)); // true
        System.out.println(service.allowRequest("alice", UserTier.FREE)); // false

        // Bob has his own quota.
        System.out.println(service.allowRequest("bob", UserTier.FREE));   // true

        System.out.println(service.allowRequest("eve", UserTier.PREMIUM)); // true
    }
}
