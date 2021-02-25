package dk.ronin;

import com.google.common.util.concurrent.Uninterruptibles;
import dk.ronin.functional.Limiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class AreaCalculationService {

    private final Limiter onlyOneCallLimiter;

    public AreaV1Response rectangle(String apiKey, RectangleDimensionsV1 dimensions) {
        AreaV1Response invokeResponse = onlyOneCallLimiter.invoke(apiKey, dimensions, rectangleDimensionsV1 -> {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            return AreaV1Response.builder()
                    .success(true)
                    .response(new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth()))
                    .build();
        });

        return invokeResponse;
    }
}
/*
*****************************
***    imperative style   ***
*****************************

    private final RateLimitConfig rateLimitConfig;
    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    public AreaV1 imperativeRectangle(String apiKey, RectangleDimensionsV1 dimensions) throws InterruptedException, TooManyRequestException {
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

*/


