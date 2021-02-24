package dk.ronin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

// based on https://techblog.bozho.net/basic-api-rate-limiting/

// curl -X POST http://localhost:8080/api/v1/area/rectangle -H "Content-Type: application/json" -d '{ "length": 10, "width": 12 }'
//{"shape":"rectangle","area":120.0}%x
@Slf4j
@RestController
@SpringBootApplication
@RequiredArgsConstructor
public class AreaCalculationControllerLambda {

    public static void main(String[] args) {
        SpringApplication.run(AreaCalculationControllerLambda.class, args);
    }

    private final AreaCalculationService service;

    @PostMapping(value = "/api/v1/area/rectangle")
    public ResponseEntity<?> rectangle(@RequestBody RectangleDimensionsV1 dimensions, HttpServletRequest request) throws InterruptedException, IOException {
        String apiKey = request.getHeader("X-api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing Header: X-api-key");
        }
        try {
            AreaV1 rectangle = service.rectangle(apiKey, dimensions);
            return ResponseEntity.ok(rectangle);
        } catch (TooManyRequestException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("You have exhausted your API Request Quota for apiKey=" + apiKey);
        }
    }
}
