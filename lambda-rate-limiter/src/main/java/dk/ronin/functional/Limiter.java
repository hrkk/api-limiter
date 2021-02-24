package dk.ronin.functional;

import dk.ronin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    public void invokeConsume(Integer key, Consumer<Integer> action) {
        // ArrayList list = new ArrayList();
        action.accept(key);
    }

    public String invokeFuntion(Integer key, Function<Integer, Integer> action) {
        Integer apply = action.apply(key);
        return "" + apply;
    }

    public AreaV1 limit(String apiKey, RectangleDimensionsV1 request, Function<RectangleDimensionsV1, AreaV1> action) throws TooManyRequestException {
        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter("rectangle/" + apiKey);
        boolean allowRequest = rateLimiter.tryAcquire();
        if (!allowRequest) {
            throw new TooManyRequestException("Too Many Requests");
        }
        AreaV1 areaV1 = action.apply(request);
        rateLimiter.release();
        return areaV1;
    }

    public SimpleRateLimiter resolveSimpleRateLimiter(String apiKey) {
        return limiters.computeIfAbsent(apiKey, this::newRateLimiter);
    }

    private SimpleRateLimiter newRateLimiter(String apiKey) {
        log.info("Creating rate limiter for apiKey={} with tokens {} pr 1 minute", apiKey, rateLimitConfig.getTokens());
        return SimpleRateLimiter.create(rateLimitConfig.getTokens(), TimeUnit.MINUTES);
    }
}
