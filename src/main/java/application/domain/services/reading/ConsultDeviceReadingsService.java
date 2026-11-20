package application.domain.services.reading;

import application.domain.exceptions.DeviceReadingNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.models.DeviceReading;
import application.domain.ports.in.ConsultDeviceReadingsUseCase;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F3 - queries the readings registered so far.
 */
@Service
@RequiredArgsConstructor
public class ConsultDeviceReadingsService implements ConsultDeviceReadingsUseCase {

    private final DeviceReadingRepositoryPort deviceReadingRepositoryPort;

    @Override
    public List<DeviceReading> consultAll() {
        return deviceReadingRepositoryPort.findAll();
    }

    @Override
    public DeviceReading consult(SurveillanceDevice device) {
        if (device == null) {
            throw new DomainException("Device is required.");
        }
        return deviceReadingRepositoryPort.findBy(device)
                .orElseThrow(() -> new DeviceReadingNotFoundException(device));
    }
}
