package dk.ronin.functional;

import com.google.common.util.concurrent.Uninterruptibles;
import dk.ronin.AreaV1;
import dk.ronin.RateLimitConfig;
import dk.ronin.RectangleDimensionsV1;
import dk.ronin.TooManyRequestException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) throws TooManyRequestException {
        // Consumer to display a number
        Consumer<Integer> display = a -> System.out.println(a);

        // Implement display using accept()
        display.accept(10);

        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setEnabled(true);
        rateLimitConfig.setTokens(1);
        Limiter limiter = new Limiter(rateLimitConfig);
        limiter.invokeConsume(7, display);


        String res = limiter.invokeFuntion(7, integer -> 3 + integer);

        System.out.println("invokeFuntion = " + res);
        RectangleDimensionsV1 request = new RectangleDimensionsV1();
        request.setLength(2);
        request.setLength(4);


        AreaV1 area = limiter.limit("030", request, dimensions -> {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            AreaV1 areaV1 = new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth());
            return areaV1;
        });
        System.out.println(area);
    }
}
