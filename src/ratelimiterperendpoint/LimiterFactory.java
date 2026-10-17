package ratelimiterperendpoint;

import java.util.Collections;
import java.util.Map;

public final class LimiterFactory {
    //private constructor to prevent unnecessary factory instances
    private LimiterFactory(){}

    public static Limiter create(Map<String, Object> config){
        String algorithm = (String) config.get("algorithm");
        @SuppressWarnings("unchecked")
        Map<String, Object> algoConfig = (Map<String, Object>) config.get("algoConfig");

        if(algoConfig == null){
            algoConfig = Collections.emptyMap();
        }

        if("TokenBucket".equals(algorithm)){
            int capacity = ((Number) algoConfig.getOrDefault("capacity", 0)).intValue();
            int refillRate = ((Number) algoConfig.getOrDefault("refillRatePerSecond", 0)).intValue();
            return new TokenBucketRateLimiter(capacity, refillRate);
        }

        throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
    }
}
