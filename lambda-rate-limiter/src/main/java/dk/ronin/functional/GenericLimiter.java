package dk.ronin.functional;

import dk.ronin.RateLimitConfig;
import dk.ronin.SimpleRateLimiter;
import dk.ronin.TooManyRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class GenericLimiter<K,V>{

    private final RateLimitConfig rateLimitConfig;

    private final Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    public V invoke2(K key, Function<? super K, ? extends V> action) {
        V result = action.apply(key);
        return result;
    }

    public V invoke(String apiKey, K request,  Function<? super K, ? extends V> action) throws TooManyRequestException {
        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter(apiKey);
        boolean allowRequest = rateLimiter.tryAcquire();
        if (!allowRequest) {
            throw new TooManyRequestException("Too Many Requests");
        }
        V result = action.apply(request);
        rateLimiter.release();
        return result;
    }


    public SimpleRateLimiter resolveSimpleRateLimiter(String apiKey) {
        return limiters.computeIfAbsent(apiKey, this::newRateLimiter);
    }

    private SimpleRateLimiter newRateLimiter(String apiKey) {
        log.info("Creating rate limiter for apiKey={} with tokens {} pr 1 minute", apiKey, rateLimitConfig.getTokens());
        return SimpleRateLimiter.create(rateLimitConfig.getTokens(), TimeUnit.MINUTES);
    }



}
