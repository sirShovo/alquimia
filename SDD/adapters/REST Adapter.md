# REST Adapter

Location: `application.adapters.in.rest`. Base path: `/api`. All responses use the envelope:

```json
{ "success": true, "message": "…", "data": { }, "timestamp": "2026-11-18T10:00:00" }
```

`deviceName` path variables accept codes (`star-labs`, `ccpd`, `star-satellite`) or display names
(lenient match — see `SurveillanceDevice.fromName`).

---

## Endpoints

| Req | Method | Path | Body | Success |
|-----|--------|------|------|---------|
| F1 | POST | `/api/interceptions` | `FullInterceptionRequest` | 200 `InterceptionResponse` |
| F2 | POST | `/api/devices/{deviceName}/readings` | `DeviceReadingRequest` | 201 `DeviceReadingResponse` |
| F3 | GET | `/api/devices/readings` | — | 200 `DeviceReadingResponse[]` |
| F3 | GET | `/api/devices/{deviceName}/readings` | — | 200 `DeviceReadingResponse` |
| F3 | GET | `/api/interceptions/accumulated` | — | 200 `InterceptionResponse` |
| F4 | PUT | `/api/devices/{deviceName}/readings` | `DeviceReadingRequest` | 200 `DeviceReadingResponse` |
| F5 | PATCH | `/api/devices/{deviceName}/readings` | `PartialDeviceReadingRequest` | 200 `DeviceReadingResponse` |
| F6 | DELETE | `/api/devices/{deviceName}/readings` | — | 200 (data `null`) |

---

## Requests

```jsonc
// FullInterceptionRequest (F1)
{ "devices": [
    { "name": "star-labs",      "distance": 485.41, "message": ["savitar", "", "", "the", "lab"] },
    { "name": "ccpd",           "distance": 265.75, "message": ["", "returns", "", "the", ""] },
    { "name": "star-satellite", "distance": 600.52, "message": ["", "", "returns", "at", "", ""] }
] }

// DeviceReadingRequest (F2, F4) — both fields required
{ "distance": 265.75, "message": ["", "returns", "", "the", ""] }

// PartialDeviceReadingRequest (F5) — at least one field
{ "distance": 270.0 }
```

Bean Validation: `devices` not empty, each `name` not blank; `distance` not null and `>= 0`; `message`
not null with no `null` elements.

## Responses

```jsonc
// InterceptionResponse
{ "position": { "x": -100.0, "y": 75.0 }, "message": "savitar returns at the lab" }

// DeviceReadingResponse
{ "device": "ccpd", "deviceName": "CCPD", "devicePosition": { "x": 100.0, "y": -100.0 },
  "distance": 265.75, "message": ["", "returns", "", "the", ""] }
```

---

## Error Mapping (`GlobalExceptionHandler`)

| Cause | Status | `data` |
|-------|--------|--------|
| `MethodArgumentNotValidException` | 400 | `{ field: message }` |
| `HttpMessageNotReadableException` (malformed JSON) | 400 | — |
| `DomainException` | 400 | — |
| `UnknownDeviceException`, `DeviceReadingNotFoundException` | 404 | — |
| `DeviceReadingAlreadyRegisteredException` | 409 | — |
| `InsufficientDataException` | 422 | `{ "missingDevices": [...] }` |
| `LocationUndeterminableException`, `MessageUndeterminableException` | 422 | — |
| Any other exception | 500 | — (logged) |

---

## Mappers

`InterceptionRestMapper` and `DeviceReadingRestMapper` convert DTOs into domain objects
(`SurveillanceDevice.fromName`, `new Distance(..)`, `new MessageFragment(..)`) and domain objects into
responses. Controllers contain no business logic.
