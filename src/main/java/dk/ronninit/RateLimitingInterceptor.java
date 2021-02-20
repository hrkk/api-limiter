package dk.ronninit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptor.class);

   // @Value("${rate.limit.enabled}")
    private boolean enabled = true;

    //@Value("${rate.limit.hourly.limit}")
    private int hourlyLimit = 1;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private Map<String, Optional<SimpleRateLimiter>> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!enabled) {
            return true;
        }
        String clientId = request.getHeader("Client-Id");
        clientId="020";
        // let non-API requests pass
        if (clientId == null) {
            return true;
        }
        SimpleRateLimiter rateLimiter = getRateLimiter(clientId);
        boolean allowRequest = rateLimiter.tryAcquire();

        if (!allowRequest) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }
        response.addHeader("X-RateLimit-Limit", String.valueOf(hourlyLimit));
        return allowRequest;
    }

    private SimpleRateLimiter getRateLimiter(String clientId2) {
        String clientId = clientId2;
        Optional<SimpleRateLimiter> simpleRateLimiter = limiters.computeIfAbsent(clientId2, applicationId -> Optional.of(createRateLimiter(applicationId)));

        return simpleRateLimiter.get();

    }

    private SimpleRateLimiter createRateLimiter(String applicationId) {
        logger.info("Creating rate limiter for applicationId={}", applicationId);
        return SimpleRateLimiter.create(hourlyLimit, TimeUnit.MINUTES); //, scheduler, applicationId);
    }


    @PreDestroy
    public void destroy() {
        // loop and finalize all limiters
        scheduler.shutdown();
    }
}