package ratelimiterperendpoint;

import ratelimiter.RateLimiterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RateLimiter {
    private final Map<String, Limiter> limiters;
    private final Limiter defaultLimiter;


    public RateLimiter(List<Map<String, Object>> configs, Map<String, Object> defaultConfig) {
        this.limiters = new HashMap<>();

        for(Map<String, Object> config : configs){
            String endpoint = (String) config.get("endpoint");
            if (endpoint == null) {
                continue;
            }

            Limiter limiter = LimiterFactory.create(config);
            limiters.put(endpoint, limiter);
        }

        this.defaultLimiter = LimiterFactory.create(defaultConfig);
    }

    public RateLimiterResult allow(String clientId, String endpoint){
        Limiter limiter = limiters.getOrDefault(endpoint, defaultLimiter);
        return limiter.allow(clientId);
    }
}
