package ronninit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class RateLimitingInterceptor extends HandlerInterceptorAdapter {

    private final RateLimitConfig rateLimitConfig;

    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }
        String apiKey = request.getHeader("X-api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Header: X-api-key");
            return false;
        }

        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter(apiKey);
        boolean allowRequest = rateLimiter.tryAcquire();
        if (allowRequest) {
            response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getTokens()));
            return true;
        } else {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "You have exhausted your API Request Quota");
            return false;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        String apiKey = request.getHeader("X-api-key");
        SimpleRateLimiter rateLimiter = resolveSimpleRateLimiter(apiKey);
        log.info("service called ended for apiKey={} - open for request again for this key by adding permits by one", apiKey);
        rateLimiter.release();
    }

    public SimpleRateLimiter resolveSimpleRateLimiter(String apiKey) {
        return limiters.computeIfAbsent(apiKey, this::newRateLimiter);
    }

    private SimpleRateLimiter newRateLimiter(String apiKey) {
        log.info("Creating rate limiter for apiKey={} with tokens {} pr 1 minute", apiKey, rateLimitConfig.getTokens());
        return SimpleRateLimiter.create(rateLimitConfig.getTokens(), TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        // loop and finalize all limiters
        log.info("destroy");
        limiters.values().forEach(rateLimiter -> rateLimiter.stop());
    }
}