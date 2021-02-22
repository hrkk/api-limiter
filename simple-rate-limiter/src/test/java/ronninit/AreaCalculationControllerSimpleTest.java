package ronninit;

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

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AreaCalculationControllerSimpleTest {

    public static final String URL = "http://localhost:8080/api/v1/area/rectangle";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testAllDifferentClientIds() throws InterruptedException {
        //  given
        Request request = Request.builder().length(10).width(5).build();
        List<String> results = new ArrayList<>();

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.execute(() -> {
                log.info("Asynchronous task");
                ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity(request, headers(finalI)), String.class);
                log.info("Asynchronous task {} {}", exchange.getStatusCodeValue(), exchange.getBody());
                results.add(exchange.getBody());
            });
        }
        Thread.sleep(10000);
        // then
        Assertions.assertEquals(100, results.size());
        executorService.shutdown();

    }

    @Test
    public void testSameClientIds() throws InterruptedException {
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
                    ResponseEntity<String> exchange = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity(request, headers(finalI)), String.class);
                    log.info("Asynchronous task {} {}", exchange.getStatusCodeValue(), exchange.getBody());
                    results.add(exchange.getBody());
                } catch (Exception e) {
                    log.info("Asynchronous task failed {}", e.getMessage());
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
        headers.add("Client-Id", "030" + i);
        return headers;
    }
}

@Builder
@Data
class Request {
    private int length;
    private int width;
}