package application.domain.services.reading;

import application.domain.exceptions.DeviceReadingNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.ports.in.DeleteDeviceReadingUseCase;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import application.domain.valueobjects.SurveillanceDevice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F6 - deletes the stored reading of a device without affecting the others.
 */
@Service
@RequiredArgsConstructor
public class DeleteDeviceReadingService implements DeleteDeviceReadingUseCase {

    private final DeviceReadingRepositoryPort deviceReadingRepositoryPort;

    @Override
    public void delete(SurveillanceDevice device) {
        if (device == null) {
            throw new DomainException("Device is required.");
        }
        if (!deviceReadingRepositoryPort.deleteIfPresent(device)) {
            throw new DeviceReadingNotFoundException(device);
        }
    }
}
