# Device Reading Services

Location: `application.domain.services.reading`. All services depend only on
`DeviceReadingRepositoryPort`.

---

## RegisterDeviceReadingService (F2)

```java
DeviceReading register(DeviceReading reading);
```

- `repository.saveIfAbsent(reading)`; if it returns `false` →
  `DeviceReadingAlreadyRegisteredException` (use F4 or F5 to change an existing reading).
- Readings may arrive in any order; each one is stored independently.

## ConsultDeviceReadingsService (F3 – query)

```java
List<DeviceReading> consultAll();                 // ordered by catalog order; may be empty
DeviceReading consult(SurveillanceDevice device); // DeviceReadingNotFoundException if absent
```

## ReplaceDeviceReadingService (F4)

```java
DeviceReading replace(DeviceReading reading);
```

- Both distance and fragment are required (guaranteed by `DeviceReading` invariants).
- `repository.replaceIfPresent(reading)`; empty → `DeviceReadingNotFoundException`.
- Only the key of `reading.device()` is touched.

## UpdateDeviceReadingPartiallyService (F5)

```java
DeviceReading update(SurveillanceDevice device, Distance distance, MessageFragment fragment);
```

- `distance` and `fragment` are optional (`null` = not provided); both null → `DomainException`
  ("at least one field must be provided").
- `repository.updateIfPresent(device, current -> merge(current))` where `merge` applies
  `withDistance` / `withFragment` only for the provided values; empty → `DeviceReadingNotFoundException`.
- Differs from F4: omitted fields keep their previous value.

## DeleteDeviceReadingService (F6)

```java
void delete(SurveillanceDevice device);
```

- `repository.deleteIfPresent(device)`; `false` → `DeviceReadingNotFoundException`.
- Other devices remain untouched.
