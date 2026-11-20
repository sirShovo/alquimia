package application.domain.services.reading;

import application.domain.exceptions.DeviceReadingNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.models.DeviceReading;
import application.domain.ports.in.UpdateDeviceReadingPartiallyUseCase;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F5 - updates only the provided fields of a device reading; omitted fields keep their previous value.
 * The merge is applied atomically by the repository.
 */
@Service
@RequiredArgsConstructor
public class UpdateDeviceReadingPartiallyService implements UpdateDeviceReadingPartiallyUseCase {

    private final DeviceReadingRepositoryPort deviceReadingRepositoryPort;

    @Override
    public DeviceReading update(SurveillanceDevice device, Distance distance, MessageFragment fragment) {
        if (device == null) {
            throw new DomainException("Device is required.");
        }
        if (distance == null && fragment == null) {
            throw new DomainException("At least one field (distance or message) must be provided.");
        }
        return deviceReadingRepositoryPort.updateIfPresent(device, current -> {
            DeviceReading updated = current;
            if (distance != null) {
                updated = updated.withDistance(distance);
            }
            if (fragment != null) {
                updated = updated.withFragment(fragment);
            }
            return updated;
        }).orElseThrow(() -> new DeviceReadingNotFoundException(device));
    }
}
