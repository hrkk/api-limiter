package dk.ronin;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AreaCalculationControllerLambdaTest {

    public static final String URL = "http://localhost:8080/api/v1/area/rectangle";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void test_with_100_different_api_keys() throws InterruptedException {
        //  given
        Request request = Request.builder().length(10).width(5).build();
        List<String> results = new ArrayList<>();

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.execute(() -> {
                log.info("BEIGN Asynchronous task");
                ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity(request, headers(finalI)), String.class);
                log.info("END Asynchronous task {} {}", exchange.getStatusCodeValue(), exchange.getBody());
                results.add(exchange.getBody());
            });
        }
        Thread.sleep(10000);
        // then
        Assertions.assertEquals(100, results.size());

        // and then make sure we can call again
        results.clear();
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.execute(() -> {
                log.info("BEIGN Asynchronous task");
                ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity(request, headers(finalI)), String.class);
                log.info("END Asynchronous task {} {}", exchange.getStatusCodeValue(), exchange.getBody());
                results.add(exchange.getBody());
            });
        }
        Thread.sleep(10000);
        executorService.shutdown();
        Assertions.assertEquals(100, results.size());

    }

    @Test
    public void test_with_10_same_api_keys() throws InterruptedException {
        //  given
        Request request = Request.builder().length(10).width(5).build();
        List<String> results = new ArrayList<>();
        List<String> fails = new ArrayList<>();

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            int finalI = 1;
            executorService.execute(() -> {
                try {
                    log.info("BEIGN Asynchronous task");
                    ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity(request, headers(finalI)), String.class);
                    log.info("END Asynchronous task {} {}", exchange.getStatusCodeValue(), exchange.getBody());
                    results.add(exchange.getBody());
                } catch (Exception e) {
                    log.info("END Asynchronous task failed {}", e.getMessage());
                    fails.add(e.getMessage());
                }

            });
        }
        Thread.sleep(6000);
        // then
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(9, fails.size());
        executorService.shutdown();
    }

    private MultiValueMap<String, String> headers(int i) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT_CHARSET, "charset=UTF-8");
        headers.add("X-api-key", "030" + i);
        return headers;
    }
}

@Builder
@Data
class Request {
    private int length;
    private int width;
}

