package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Body of F1: the information of the three devices received at once.
 */
public record FullInterceptionRequest(
        @NotEmpty(message = "devices is required")
        List<@NotNull(message = "device entries must not be null") @Valid DeviceInterceptionRequest> devices
) {
}
