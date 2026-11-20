# Output Ports

Location: `application.domain.ports.out`.

## DeviceReadingRepositoryPort

Temporary storage of device readings (one per device). The contract is expressed with atomic,
single-key operations so that the domain can guarantee consistency (BR06) without knowing how the
storage achieves it.

```java
public interface DeviceReadingRepositoryPort {

    List<DeviceReading> findAll();

    Optional<DeviceReading> findBy(SurveillanceDevice device);

    /** Stores the reading only if the device has none. @return true if stored. */
    boolean saveIfAbsent(DeviceReading reading);

    /** Replaces the reading only if the device already has one. */
    Optional<DeviceReading> replaceIfPresent(DeviceReading reading);

    /** Atomically applies {@code updater} to the current reading, if any. */
    Optional<DeviceReading> updateIfPresent(SurveillanceDevice device, UnaryOperator<DeviceReading> updater);

    /** @return true if a reading existed and was removed. */
    boolean deleteIfPresent(SurveillanceDevice device);
}
```

Implemented by `application.adapters.out.memory.InMemoryDeviceReadingAdapter`.
