package application.domain.services.interception;

import application.domain.exceptions.InsufficientDataException;
import application.domain.models.DeviceReading;
import application.domain.models.InterceptionResult;
import application.domain.ports.in.ProcessAccumulatedInterceptionUseCase;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F3 - processes the readings accumulated through individual registrations. Stored data is not modified.
 */
@Service
@RequiredArgsConstructor
public class ProcessAccumulatedInterceptionService implements ProcessAccumulatedInterceptionUseCase {

    private final DeviceReadingRepositoryPort deviceReadingRepositoryPort;
    private final InterceptionCalculationService interceptionCalculationService;

    @Override
    public InterceptionResult process() {
        Map<SurveillanceDevice, DeviceReading> byDevice = new EnumMap<>(SurveillanceDevice.class);
        for (DeviceReading reading : deviceReadingRepositoryPort.findAll()) {
            byDevice.put(reading.device(), reading);
        }
        List<SurveillanceDevice> missing = Stream.of(SurveillanceDevice.values())
                .filter(device -> !byDevice.containsKey(device))
                .toList();
        if (!missing.isEmpty()) {
            throw new InsufficientDataException(missing);
        }
        return interceptionCalculationService.calculate(byDevice);
    }
}
