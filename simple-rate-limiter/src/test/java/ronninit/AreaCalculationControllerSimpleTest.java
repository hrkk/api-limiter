package ronninit;

import lombok.Builder;
import lombok.Data;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AreaCalculationControllerSimpleTest {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testAllDifferentClientIds() throws InterruptedException {
        //  given
        Request request = Request.builder().length(10).width(5).build();
        List<String> results = new ArrayList<>();

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executorService.execute(() -> {
                System.out.println("Asynchronous task");
                ResponseEntity<String> exchange = restTemplate.exchange("http://localhost:8080/api/v1/area/rectangle", HttpMethod.POST, new HttpEntity(request, headers(finalI)), String.class);
                System.out.println("Asynchronous task " + exchange.getStatusCodeValue() + " " + exchange.getBody());
                results.add(exchange.getBody());
            });
        }
        Thread.sleep(10000);
        // then
        Assertions.assertEquals(10, results.size());
        executorService.shutdown();

    }

    private MultiValueMap<String, String> headers(int i) {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT_CHARSET, "charset=UTF-8");
        headers.add("Client-Id", "030"+i);

        return headers;
    }
}

@Builder
@Data
class Request {
    private int length;
    private int width;
}