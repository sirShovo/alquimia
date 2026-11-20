package application.adapters.out.memory;

import application.domain.models.DeviceReading;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Component;

/**
 * Temporary in-memory storage of device readings. Data lives as long as the application process.
 * Every operation is atomic per device thanks to {@link ConcurrentHashMap}.
 */
@Component
public class InMemoryDeviceReadingAdapter implements DeviceReadingRepositoryPort {

    private final Map<SurveillanceDevice, DeviceReading> readings = new ConcurrentHashMap<>();

    @Override
    public List<DeviceReading> findAll() {
        return readings.values().stream()
                .sorted(Comparator.comparing(DeviceReading::device))
                .toList();
    }

    @Override
    public Optional<DeviceReading> findBy(SurveillanceDevice device) {
        return Optional.ofNullable(readings.get(device));
    }

    @Override
    public boolean saveIfAbsent(DeviceReading reading) {
        return readings.putIfAbsent(reading.device(), reading) == null;
    }

    @Override
    public Optional<DeviceReading> replaceIfPresent(DeviceReading reading) {
        return Optional.ofNullable(readings.computeIfPresent(reading.device(), (device, current) -> reading));
    }

    @Override
    public Optional<DeviceReading> updateIfPresent(SurveillanceDevice device, UnaryOperator<DeviceReading> updater) {
        return Optional.ofNullable(readings.computeIfPresent(device, (key, current) -> updater.apply(current)));
    }

    @Override
    public boolean deleteIfPresent(SurveillanceDevice device) {
        return readings.remove(device) != null;
    }
}
