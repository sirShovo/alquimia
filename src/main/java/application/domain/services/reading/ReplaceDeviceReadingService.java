package application.domain.services.reading;

import application.domain.exceptions.DeviceReadingNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.models.DeviceReading;
import application.domain.ports.in.ReplaceDeviceReadingUseCase;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F4 - fully replaces the registered reading of a device (distance and full fragment). Other devices
 * are not affected.
 */
@Service
@RequiredArgsConstructor
public class ReplaceDeviceReadingService implements ReplaceDeviceReadingUseCase {

    private final DeviceReadingRepositoryPort deviceReadingRepositoryPort;

    @Override
    public DeviceReading replace(DeviceReading reading) {
        if (reading == null) {
            throw new DomainException("Reading is required.");
        }
        return deviceReadingRepositoryPort.replaceIfPresent(reading)
                .orElseThrow(() -> new DeviceReadingNotFoundException(reading.device()));
    }
}
