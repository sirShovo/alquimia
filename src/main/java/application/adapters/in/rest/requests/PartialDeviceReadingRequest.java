package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Body of F5 (partial update): every field is optional, omitted fields keep their previous value.
 * The domain requires at least one of them.
 */
public record PartialDeviceReadingRequest(
        @PositiveOrZero(message = "distance must be greater than or equal to zero")
        Double distance,

        List<@NotNull(message = "message words must not be null; use \"\" for missing words") String> message
) {
}
