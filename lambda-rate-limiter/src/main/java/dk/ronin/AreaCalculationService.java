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
