# Input Ports

Input ports are the **use-case contracts** invoked by the REST adapter. Conventions (same as the
reference project):

- One interface per use case, named `<Verb><Noun>UseCase`.
- Parameters / return values are Domain Models or Value Objects, never DTOs.
- Each interface is implemented by exactly one domain service (`<same name>Service`).

Location: `application.domain.ports.in`.

| Req | Interface | Contract | Service |
|-----|-----------|----------|---------|
| F1 | `ProcessFullInterceptionUseCase` | `InterceptionResult process(List<DeviceReading> readings)` | `ProcessFullInterceptionService` |
| F2 | `RegisterDeviceReadingUseCase` | `DeviceReading register(DeviceReading reading)` | `RegisterDeviceReadingService` |
| F3 | `ConsultDeviceReadingsUseCase` | `List<DeviceReading> consultAll()`<br>`DeviceReading consult(SurveillanceDevice device)` | `ConsultDeviceReadingsService` |
| F3 | `ProcessAccumulatedInterceptionUseCase` | `InterceptionResult process()` | `ProcessAccumulatedInterceptionService` |
| F4 | `ReplaceDeviceReadingUseCase` | `DeviceReading replace(DeviceReading reading)` | `ReplaceDeviceReadingService` |
| F5 | `UpdateDeviceReadingPartiallyUseCase` | `DeviceReading update(SurveillanceDevice device, Distance distance, MessageFragment fragment)` | `UpdateDeviceReadingPartiallyService` |
| F6 | `DeleteDeviceReadingUseCase` | `void delete(SurveillanceDevice device)` | `DeleteDeviceReadingService` |

`ConsultDeviceReadingsUseCase` groups the two read operations of F3 (list and single), as they share
the same purpose and dependencies.
