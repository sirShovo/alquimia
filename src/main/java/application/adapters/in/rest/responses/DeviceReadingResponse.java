package application.adapters.in.rest.responses;

import java.util.List;

/**
 * Stored reading of one device.
 */
public record DeviceReadingResponse(
        String device,
        String deviceName,
        CoordinatesResponse devicePosition,
        double distance,
        List<String> message
) {
}
