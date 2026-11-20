package application.domain.services.interception;

import application.domain.exceptions.DomainException;
import application.domain.models.DeviceReading;
import application.domain.models.InterceptionResult;
import application.domain.ports.in.ProcessFullInterceptionUseCase;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * F1 - processes the information of the three devices received at once. The readings are not stored.
 */
@Service
@RequiredArgsConstructor
public class ProcessFullInterceptionService implements ProcessFullInterceptionUseCase {

    private final InterceptionCalculationService interceptionCalculationService;

    @Override
    public InterceptionResult process(List<DeviceReading> readings) {
        if (readings == null || readings.isEmpty()) {
            throw new DomainException("The readings of the three devices are required.");
        }
        Map<SurveillanceDevice, DeviceReading> byDevice = new EnumMap<>(SurveillanceDevice.class);
        for (DeviceReading reading : readings) {
            if (reading == null) {
                throw new DomainException("Readings must not be null.");
            }
            if (byDevice.putIfAbsent(reading.device(), reading) != null) {
                throw new DomainException(
                        "Device '" + reading.device().getCode() + "' was sent more than once.");
            }
        }
        if (byDevice.size() != SurveillanceDevice.values().length) {
            String missing = Stream.of(SurveillanceDevice.values())
                    .filter(device -> !byDevice.containsKey(device))
                    .map(SurveillanceDevice::getCode)
                    .collect(Collectors.joining(", "));
            throw new DomainException(
                    "Full processing requires the readings of the three devices. Missing: " + missing + ".");
        }
        return interceptionCalculationService.calculate(byDevice);
    }
}
