# Domain Services

Location: `application.domain.services`. Services are Spring `@Service` beans with constructor injection
(`@RequiredArgsConstructor`), but contain no HTTP or storage code; they are unit-testable by direct
instantiation.

| Package | Service | Kind | Requirement |
|---------|---------|------|-------------|
| `interception` | `TrilaterationService` | Pure domain calculation | F1, F3 |
| `interception` | `MessageReconstructionService` | Pure domain calculation | F1, F3 |
| `interception` | `InterceptionCalculationService` | Combines both calculations over one complete set of readings | F1, F3 |
| `interception` | `ProcessFullInterceptionService` | Use case | F1 |
| `interception` | `ProcessAccumulatedInterceptionService` | Use case | F3 |
| `reading` | `RegisterDeviceReadingService` | Use case | F2 |
| `reading` | `ConsultDeviceReadingsService` | Use case | F3 |
| `reading` | `ReplaceDeviceReadingService` | Use case | F4 |
| `reading` | `UpdateDeviceReadingPartiallyService` | Use case | F5 |
| `reading` | `DeleteDeviceReadingService` | Use case | F6 |

Detailed behavior:

- [interception-services.md](services/interception-services.md)
- [device-reading-services.md](services/device-reading-services.md)

## Design Principles

- **Domain Model parameters:** use cases receive `DeviceReading`, `SurveillanceDevice`, `Distance`,
  `MessageFragment` — never request DTOs or raw JSON.
- **External information** only through `DeviceReadingRepositoryPort`.
- **Calculation services** (`TrilaterationService`, `MessageReconstructionService`) are stateless and
  reused by both F1 and F3, so both paths produce identical results.
