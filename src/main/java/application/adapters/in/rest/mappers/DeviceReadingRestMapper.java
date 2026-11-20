package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.DeviceReadingRequest;
import application.adapters.in.rest.responses.CoordinatesResponse;
import application.adapters.in.rest.responses.DeviceReadingResponse;
import application.domain.models.DeviceReading;
import application.domain.valueobjects.Coordinates;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Translates device reading DTOs to domain objects and back.
 */
@Component
public class DeviceReadingRestMapper {

    public SurveillanceDevice toDevice(String deviceName) {
        return SurveillanceDevice.fromName(deviceName);
    }

    public DeviceReading toDomain(String deviceName, DeviceReadingRequest request) {
        return new DeviceReading(toDevice(deviceName), toDistance(request.distance()), toFragment(request.message()));
    }

    /** @return {@code null} when the distance was not provided. */
    public Distance toDistance(Double distance) {
        return distance == null ? null : new Distance(distance);
    }

    /** @return {@code null} when the message was not provided. */
    public MessageFragment toFragment(List<String> message) {
        return message == null ? null : new MessageFragment(message);
    }

    public DeviceReadingResponse toResponse(DeviceReading reading) {
        SurveillanceDevice device = reading.device();
        return new DeviceReadingResponse(
                device.getCode(),
                device.getDisplayName(),
                toResponse(device.getPosition()),
                reading.distance().value(),
                reading.fragment().words());
    }

    public List<DeviceReadingResponse> toResponses(List<DeviceReading> readings) {
        return readings.stream().map(this::toResponse).toList();
    }

    private CoordinatesResponse toResponse(Coordinates coordinates) {
        return new CoordinatesResponse(coordinates.x(), coordinates.y());
    }
}
