package dk.ronin;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AreaV1Response {
    private boolean success;
    private AreaV1 response;
    private String errorMessage;
}
