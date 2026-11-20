package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Information of one device inside a full interception request (F1).
 */
public record DeviceInterceptionRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotNull(message = "distance is required")
        @PositiveOrZero(message = "distance must be greater than or equal to zero")
        Double distance,

        @NotNull(message = "message is required")
        List<@NotNull(message = "message words must not be null; use \"\" for missing words") String> message
) {
}
