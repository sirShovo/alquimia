package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Body of F2 (register) and F4 (full replacement): both fields are required.
 */
public record DeviceReadingRequest(
        @NotNull(message = "distance is required")
        @PositiveOrZero(message = "distance must be greater than or equal to zero")
        Double distance,

        @NotNull(message = "message is required")
        List<@NotNull(message = "message words must not be null; use \"\" for missing words") String> message
) {
}
