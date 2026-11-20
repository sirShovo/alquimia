package application.domain.services.reading;

import application.domain.exceptions.DeviceReadingAlreadyRegisteredException;
import application.domain.exceptions.DomainException;
import application.domain.models.DeviceReading;
import application.domain.ports.in.RegisterDeviceReadingUseCase;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F2 - registers the reading of one device. Readings may arrive in any order and at any time.
 */
@Service
@RequiredArgsConstructor
public class RegisterDeviceReadingService implements RegisterDeviceReadingUseCase {

    private final DeviceReadingRepositoryPort deviceReadingRepositoryPort;

    @Override
    public DeviceReading register(DeviceReading reading) {
        if (reading == null) {
            throw new DomainException("Reading is required.");
        }
        if (!deviceReadingRepositoryPort.saveIfAbsent(reading)) {
            throw new DeviceReadingAlreadyRegisteredException(reading.device());
        }
        return reading;
    }
}
