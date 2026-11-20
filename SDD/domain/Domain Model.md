# Domain Model

Location: `application.domain.models`.

---

## DeviceReading (aggregate root)

The information a surveillance device reported about Alchemy's lab. There is **at most one reading per
device**; the device is the identity of the aggregate.

```text
DeviceReading (record, immutable)
 ├── device   : SurveillanceDevice   (identity, required)
 ├── distance : Distance             (required)
 └── fragment : MessageFragment      (required)
```

Behavior:

```java
DeviceReading withDistance(Distance distance);   // used by F5
DeviceReading withFragment(MessageFragment f);   // used by F5
```

Invariants: no component is `null` (`DomainException`).

Lifecycle:

```text
           register (F2)            replace (F4) / updatePartially (F5)
 (absent) ───────────────▶ (present) ──────────────────────────────┐
     ▲                         │  ▲                                 │
     │       delete (F6)       │  └─────────────────────────────────┘
     └─────────────────────────┘
```

---

## InterceptionResult

Outcome of processing three readings (F1 / F3).

```text
InterceptionResult (record)
 ├── position : Coordinates   (lab location, rounded to 2 decimals)
 └── message  : String        (rebuilt message, words separated by single spaces)
```

---

## Exceptions (`application.domain.exceptions`)

| Exception | Meaning | HTTP (REST adapter) |
|-----------|---------|---------------------|
| `DomainException` | Base type; invalid data / business-rule violation | 400 |
| `UnknownDeviceException` | Device name does not match the catalog | 404 |
| `DeviceReadingNotFoundException` | No reading stored for the device | 404 |
| `DeviceReadingAlreadyRegisteredException` | Registering a device that already has a reading | 409 |
| `UnprocessableInterceptionException` | Base for "cannot compute" errors | 422 |
| `InsufficientDataException` (extends Unprocessable) | Accumulated readings are missing devices; exposes `getMissingDevices()` | 422 |
| `LocationUndeterminableException` (extends Unprocessable) | Distances are inconsistent / no unique point | 422 |
| `MessageUndeterminableException` (extends Unprocessable) | Gaps or conflicting words prevent rebuilding the message | 422 |
