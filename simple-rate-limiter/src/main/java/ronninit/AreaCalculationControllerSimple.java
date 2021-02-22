package ronninit;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// based on https://techblog.bozho.net/basic-api-rate-limiting/

// curl -X POST http://localhost:8080/api/v1/area/rectangle -H "Content-Type: application/json" -H "Client-Id: 030" -d '{ "length": 10, "width": 12 }'
//{"shape":"rectangle","area":120.0}%
@Slf4j
@RestController
@SpringBootApplication
public class AreaCalculationControllerSimple {

    public static void main(String[] args) {
        SpringApplication.run(AreaCalculationControllerSimple.class, args);
    }

    @PostMapping(value = "/api/v1/area/rectangle")
    public ResponseEntity<AreaV1> rectangle(@RequestBody RectangleDimensionsV1 dimensions) throws InterruptedException {
        Thread.sleep(5000);
        return ResponseEntity.ok(new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth()));
    }


}
