package dk.ronin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class AreaCalculationService {

    private final RateLimitConfig rateLimitConfig;

    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    LimiterFunctionalInterface limiter = (limiter, dimensions1) -> {
        boolean allowRequest = limiter.tryAcquire();
        if (!allowRequest) {
            throw new TooManyRequestException("Too Many Requests");
        }
        Thread.sleep(5000);
        AreaV1 area = new AreaV1("rectangle", dimensions1.getLength() * dimensions1.getWidth());
        limiter.release();
        return area;
    };


    public AreaV1 functionalRectangle(String apiKey, RectangleDimensionsV1 dimensions) throws InterruptedException, TooManyRequestException {
        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter("rectangle/" + apiKey);
        AreaV1 areaV1 = limiter.allow(rateLimiter, dimensions);
        return areaV1;
    }

    public AreaV1 rectangle(String apiKey, RectangleDimensionsV1 dimensions) throws InterruptedException, TooManyRequestException {
        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter("rectangle/" + apiKey);
        boolean allowRequest = rateLimiter.tryAcquire();
        if (!allowRequest) {
            throw new TooManyRequestException("Too Many Requests");
        }
        Thread.sleep(5000);
        AreaV1 area = new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth());
        rateLimiter.release();
        return area;
    }

    public SimpleRateLimiter resolveSimpleRateLimiter(String apiKey) {
        return limiters.computeIfAbsent(apiKey, this::newRateLimiter);
    }

    private SimpleRateLimiter newRateLimiter(String apiKey) {
        log.info("Creating rate limiter for apiKey={} with tokens {} pr 1 minute", apiKey, rateLimitConfig.getTokens());
        return SimpleRateLimiter.create(rateLimitConfig.getTokens(), TimeUnit.MINUTES);
    }
}
