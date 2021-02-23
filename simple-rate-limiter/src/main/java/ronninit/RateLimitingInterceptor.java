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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class RateLimitingInterceptor extends HandlerInterceptorAdapter {

    private final RateLimitConfig rateLimitConfig;

    private Map<String, Optional<SimpleRateLimiter>> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }
        String clientId = request.getHeader("Client-Id");

        // let non-API requests pass
        if (clientId == null) {
            return true;
        }
        SimpleRateLimiter rateLimiter = getRateLimiter(clientId);
        boolean allowRequest = rateLimiter.tryAcquire();
        log.info("AllowRequest " + allowRequest);
        if (!allowRequest) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }
        response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getTokens()));
        return allowRequest;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        String clientId = request.getHeader("Client-Id");
        SimpleRateLimiter rateLimiter = getRateLimiter(clientId);
        System.err.println("release rate limit");
        rateLimiter.release();
    }

    private SimpleRateLimiter getRateLimiter(String clientId) {
        Optional<SimpleRateLimiter> simpleRateLimiter = limiters.computeIfAbsent(clientId, applicationId -> Optional.of(createRateLimiter(applicationId)));
        return simpleRateLimiter.get();
    }

    private SimpleRateLimiter createRateLimiter(String applicationId) {
        log.info("Creating rate limiter for applicationId={} with tokens {} pr minutes", applicationId, rateLimitConfig.getTokens());
        return SimpleRateLimiter.create(rateLimitConfig.getTokens(), TimeUnit.MINUTES); //, scheduler, applicationId);
    }

    @PreDestroy
    public void destroy() {
        // loop and finalize all limiters
        log.info("destroy");
        limiters.values().forEach(e -> e.ifPresent(rateLimiter -> rateLimiter.stop()));
    }
}