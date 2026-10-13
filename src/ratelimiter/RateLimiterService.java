package ratelimiter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterService {
    private final Map<UserTier, RateLimitConfig> tierConfigs;

    private final Map<String, RateLimiter> userLimiters;

    public RateLimiterService(Map<UserTier, RateLimitConfig> tierConfigs) {
        this.tierConfigs = Map.copyOf(tierConfigs);
        this.userLimiters = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String userId, UserTier tier){
        //validate input
        Objects.requireNonNull(userId, "User ID must not be empty");
        Objects.requireNonNull(tier, "User tier must not be empty");

        //create/get limiter for user
        RateLimiter limiter = userLimiters.computeIfAbsent(
                userId,
                id -> RateLimiterFactory.create(tierConfigs.get(tier))
        );

        return limiter.allowRequest();
    }
}
