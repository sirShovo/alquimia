# In-Memory Persistence Adapter

Location: `application.adapters.out.memory`.

## InMemoryDeviceReadingAdapter

`@Component` implementing `DeviceReadingRepositoryPort`.

- Backing store: `ConcurrentHashMap<SurveillanceDevice, DeviceReading>` (BR02 — in memory, no database).
- Data lives as long as the application process; restarting the app clears it.
- Domain objects are immutable records, so they are stored directly (no entity/mapper layer needed).

| Port method | Implementation |
|-------------|----------------|
| `findAll` | values sorted by `SurveillanceDevice` ordinal |
| `findBy` | `Optional.ofNullable(map.get(device))` |
| `saveIfAbsent` | `map.putIfAbsent(device, reading) == null` |
| `replaceIfPresent` | `map.computeIfPresent(device, (k, old) -> reading)` |
| `updateIfPresent` | `map.computeIfPresent(device, (k, old) -> updater.apply(old))` |
| `deleteIfPresent` | `map.remove(device) != null` |

Every operation is atomic per key: concurrent registrations of the same device yield exactly one `201`
and one `409`; concurrent partial updates never lose each other's fields.
