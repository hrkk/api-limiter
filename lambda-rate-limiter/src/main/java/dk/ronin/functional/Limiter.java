package dk.ronin.functional;

import dk.ronin.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
@Component
public class Limiter {

    private final RateLimitConfig rateLimitConfig;

    private final Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    public void invokeConsume(Integer key, Consumer<Integer> action) {
        // ArrayList list = new ArrayList();
        // HashMap map = new HashMap():
        // Consumer<? super E> action
        //public V computeIfAbsent(K key,
        //        Function<? super K, ? extends V> mappingFunction) {
        action.accept(key);
    }

    public String invokeFunction(Integer key, Function<Integer, Integer> action) {
        Integer apply = action.apply(key);
        return "" + apply;
    }

    public AreaV1Response invoke(String apiKey, RectangleDimensionsV1 request, Function<RectangleDimensionsV1, AreaV1Response> actionAllowed, Function<RectangleDimensionsV1, AreaV1Response> actionOr) {
        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter(apiKey);
        boolean allowRequest = rateLimiter.tryAcquire();
        if (!allowRequest) {
            return actionOr.apply(request);
        }
        try {
            return actionAllowed.apply(request);
        } finally {
            rateLimiter.release();
        }
    }

    public SimpleRateLimiter resolveSimpleRateLimiter(String apiKey) {
        return limiters.computeIfAbsent(apiKey, this::newRateLimiter);
    }

    private SimpleRateLimiter newRateLimiter(String apiKey) {
        log.info("Creating rate limiter for apiKey={} with tokens {} pr 1 minute", apiKey, rateLimitConfig.getTokens());
        return SimpleRateLimiter.create(rateLimitConfig.getTokens(), TimeUnit.MINUTES);
    }

    public void destroy() {
        // loop and finalize all limiters
        log.info("destroy");
        limiters.values().forEach(SimpleRateLimiter::stop);
    }
}


