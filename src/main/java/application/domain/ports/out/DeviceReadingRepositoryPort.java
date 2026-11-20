package application.domain.ports.out;

import application.domain.models.DeviceReading;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Temporary storage of device readings (one per device). Every write is atomic and touches a single
 * device, which keeps the stored information consistent.
 */
public interface DeviceReadingRepositoryPort {

    /** All stored readings ordered by catalog order. */
    List<DeviceReading> findAll();

    Optional<DeviceReading> findBy(SurveillanceDevice device);

    /** Stores the reading only if the device has none. @return {@code true} if it was stored. */
    boolean saveIfAbsent(DeviceReading reading);

    /** Replaces the reading only if the device already has one. */
    Optional<DeviceReading> replaceIfPresent(DeviceReading reading);

    /** Atomically applies {@code updater} to the current reading of the device, if any. */
    Optional<DeviceReading> updateIfPresent(SurveillanceDevice device, UnaryOperator<DeviceReading> updater);

    /** @return {@code true} if a reading existed and was removed. */
    boolean deleteIfPresent(SurveillanceDevice device);
}
