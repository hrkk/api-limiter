package dk.ronin;

public interface LimiterFunctionalInterface {
    AreaV1 allow(SimpleRateLimiter limiter, RectangleDimensionsV1 dimensions) throws TooManyRequestException, InterruptedException;
}
