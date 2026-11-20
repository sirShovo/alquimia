package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.DeviceInterceptionRequest;
import application.adapters.in.rest.requests.FullInterceptionRequest;
import application.adapters.in.rest.responses.CoordinatesResponse;
import application.adapters.in.rest.responses.InterceptionResponse;
import application.domain.models.DeviceReading;
import application.domain.models.InterceptionResult;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Translates interception DTOs to domain objects and back.
 */
@Component
public class InterceptionRestMapper {

    public List<DeviceReading> toDomain(FullInterceptionRequest request) {
        return request.devices().stream().map(this::toDomain).toList();
    }

    private DeviceReading toDomain(DeviceInterceptionRequest device) {
        return new DeviceReading(
                SurveillanceDevice.fromName(device.name()),
                new Distance(device.distance()),
                new MessageFragment(device.message()));
    }

    public InterceptionResponse toResponse(InterceptionResult result) {
        return new InterceptionResponse(
                new CoordinatesResponse(result.position().x(), result.position().y()),
                result.message());
    }
}
