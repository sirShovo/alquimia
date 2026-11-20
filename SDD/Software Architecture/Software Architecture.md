# Software Architecture

## Overview

The **Operation Alchemy API** is built with **Hexagonal Architecture (Ports and Adapters)** combined with
**Domain-Driven Design (DDD)**. The business domain (trilateration, message reconstruction, device
reading lifecycle) is isolated from Spring MVC, JSON and the storage mechanism.

---

## Architectural Principles

- Domain-first design.
- Dependency inversion: adapters depend on the domain, never the opposite.
- The domain never imports HTTP, servlet, Jackson or storage types.
- One input port (use case) per business operation; one service implementing it.
- Storage is reached only through an output port, so the in-memory store can be replaced (e.g. by Redis
  or a database) without touching the domain.

---

## Technology Stack

| Concern        | Technology |
|----------------|------------|
| Language       | Java 17 |
| Framework      | Spring Boot 4.1 (Spring MVC) |
| Build          | Maven (Maven Wrapper) |
| Validation     | Jakarta Bean Validation (REST adapter) + domain invariants |
| API docs       | springdoc-openapi (Swagger UI) |
| Boilerplate    | Lombok |
| Storage        | In-memory `ConcurrentHashMap` (no database, no external services) |
| Tests          | JUnit 5, AssertJ, MockMvc |

---

## Layers

```
            ┌──────────────────────────────────────────────┐
  HTTP ───▶ │ adapters.in.rest  (controllers, DTOs, mappers)│
            └───────────────┬──────────────────────────────┘
                            │ calls input ports (use cases)
            ┌───────────────▼──────────────────────────────┐
            │ domain                                        │
            │  ports.in  ◀── implemented by ── services     │
            │  models, valueobjects, exceptions             │
            │  ports.out ──────────────────────┐            │
            └──────────────────────────────────┼────────────┘
                                               │ implemented by
            ┌──────────────────────────────────▼────────────┐
            │ adapters.out.memory (InMemoryDeviceReadingAdapter)
            └───────────────────────────────────────────────┘
  infrastructure.config: ApiResponse, GlobalExceptionHandler, OpenApiConfig
```

| Layer | Responsibility |
|-------|----------------|
| **Domain** | Business rules: devices catalog, value objects with invariants, trilateration, message reconstruction, reading lifecycle (register / replace / patch / delete), errors. |
| **Input adapters** | Translate HTTP + JSON into domain objects, call use cases, translate results into responses. |
| **Output adapters** | Implement storage ports (temporary in-memory storage). |
| **Infrastructure** | Cross-cutting Spring configuration: uniform response envelope, exception → HTTP status mapping, OpenAPI. |

---

## Package Structure

```text
src/main/java/application/
├── AlchemyApplication.java
├── adapters/
│   ├── in/rest/
│   │   ├── controllers/   InterceptionController, DeviceReadingController
│   │   ├── requests/      request DTOs + Bean Validation
│   │   ├── responses/     response DTOs
│   │   └── mappers/       DTO <-> domain
│   └── out/memory/        InMemoryDeviceReadingAdapter
├── domain/
│   ├── exceptions/
│   ├── models/            DeviceReading, InterceptionResult
│   ├── valueobjects/      SurveillanceDevice, Coordinates, Distance, MessageFragment
│   ├── ports/in/          *UseCase
│   ├── ports/out/         DeviceReadingRepositoryPort
│   └── services/
│       ├── interception/  TrilaterationService, MessageReconstructionService,
│       │                  ProcessFullInterceptionService, ProcessAccumulatedInterceptionService
│       └── reading/       Register/Consult/Replace/UpdatePartially/Delete DeviceReadingService
└── infrastructure/config/ ApiResponse, GlobalExceptionHandler, OpenApiConfig
```

---

## Request Flow (example: F5 partial update)

1. `PATCH /api/devices/ccpd/readings` → `DeviceReadingController.updatePartially`.
2. Bean Validation checks the DTO; `DeviceReadingRestMapper` builds `SurveillanceDevice`, optional
   `Distance` and optional `MessageFragment`.
3. `UpdateDeviceReadingPartiallyUseCase.update(...)` (implemented by `UpdateDeviceReadingPartiallyService`)
   validates that at least one field is present and calls
   `DeviceReadingRepositoryPort.updateIfPresent(device, reading -> merged)`.
4. `InMemoryDeviceReadingAdapter` applies the merge atomically with `ConcurrentHashMap.computeIfPresent`.
5. The controller maps the result into `ApiResponse<DeviceReadingResponse>`; domain exceptions are mapped
   to HTTP statuses by `GlobalExceptionHandler`.

---

## Consistency

All write operations go through **atomic** port methods (`saveIfAbsent`, `replaceIfPresent`,
`updateIfPresent`, `deleteIfPresent`). Each one touches a single key, so operations on one device never
affect other devices (F4/F5/F6 requirement) and concurrent requests cannot lose updates.
