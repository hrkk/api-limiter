package ronninit;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

// based on https://www.baeldung.com/spring-bucket4j
// curl -X POST http://localhost:8080/api/v1/area/rectangle -H "Content-Type: application/json" -H "Client-Id: 030" -d '{ "length": 10, "width": 12 }'
@Slf4j
@RestController
@SpringBootApplication
public class AreaCalculationControllerBucket4j {

    public static void main(String[] args) {
        SpringApplication.run(AreaCalculationControllerBucket4j.class, args);
    }

    private final Bucket bucket;

    public AreaCalculationControllerBucket4j() {
        Bandwidth limit = Bandwidth.classic(1,  Refill.greedy(1, Duration.ofSeconds(10)));
        this.bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping(value = "/api/v1/area/rectangle")
    public ResponseEntity<AreaV1> rectangle(@RequestBody RectangleDimensionsV1 dimensions) {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth()));
        }
        log.info(HttpStatus.TOO_MANY_REQUESTS.toString());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
