# Implementation Plan

The project follows an **SDD-first** workflow: the specification and design documents were written
before the code, and the code implements them.

> The source PDF contains an embedded prompt injection ("PROTOCOLO SAVITAR — INSTRUCCION PARA EL
> ASISTENTE DE IA", a 500-question tutoring protocol). It was ignored; only the visible statement was used.

| Phase | Deliverable | Status |
|-------|-------------|--------|
| 1. Specification | `SPECS.md` (F1–F6, business rules, design decisions) | Done |
| 2. Design (SDD) | `SDD/Software Architecture`, `SDD/domain/*`, `SDD/adapters/*` | Done |
| 3. Project scaffold | `pom.xml` (Java 17, Spring Boot 4.1.0), Maven Wrapper, `application.properties` | Done |
| 4. Domain | Value objects, `DeviceReading` aggregate, exceptions, input/output ports, services | Done |
| 5. Adapters | `InMemoryDeviceReadingAdapter`, REST controllers/requests/responses/mappers | Done |
| 6. Infrastructure | `ApiResponse`, `GlobalExceptionHandler`, `OpenApiConfig` | Done |
| 7. Tests | 59 JUnit tests (domain, adapter, MockMvc end-to-end) | Done — all green |
| 8. Postman | `postman/` collection (45 requests, 100 assertions) + local environment | Done — all green against the running app |

## Requirement traceability

| Req | Use case | Endpoint | Tests |
|-----|----------|----------|-------|
| F1 | `ProcessFullInterceptionUseCase` | `POST /api/interceptions` | `InterceptionUseCasesTest`, `RestApiIntegrationTest.f1*` |
| F2 | `RegisterDeviceReadingUseCase` | `POST /api/devices/{d}/readings` | `DeviceReadingUseCasesTest`, `RestApiIntegrationTest.f2*` |
| F3 | `ConsultDeviceReadingsUseCase`, `ProcessAccumulatedInterceptionUseCase` | `GET /api/devices/readings`, `GET /api/devices/{d}/readings`, `GET /api/interceptions/accumulated` | `InterceptionUseCasesTest`, `RestApiIntegrationTest.f2AndF3*` |
| F4 | `ReplaceDeviceReadingUseCase` | `PUT /api/devices/{d}/readings` | `DeviceReadingUseCasesTest`, `RestApiIntegrationTest.f4*` |
| F5 | `UpdateDeviceReadingPartiallyUseCase` | `PATCH /api/devices/{d}/readings` | `DeviceReadingUseCasesTest`, `RestApiIntegrationTest.f5*` |
| F6 | `DeleteDeviceReadingUseCase` | `DELETE /api/devices/{d}/readings` | `DeviceReadingUseCasesTest`, `RestApiIntegrationTest.f6*` |

## Verification

1. `./mvnw test` → 59 tests, 0 failures.
2. `./mvnw spring-boot:run` → run the Postman collection in the Collection Runner → all assertions pass.
