package dk.ronin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class RateLimitConfig {

    @Value("${rate.limit.enabled:true}")
    private boolean enabled;

    @Value("${rate.limit.minute.limit}")
    private int tokens;
}
