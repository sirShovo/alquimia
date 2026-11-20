# Operation Alchemy API

REST API that locates Alchemy's lab by **trilateration** and **rebuilds intercepted messages** from the
fragments reported by three surveillance devices (STAR Labs, CCPD, S.T.A.R. Satellite).

Built with **Java 17 + Spring Boot 4 + Maven**, following **Domain-Driven Design** and **Hexagonal
Architecture**. Data is kept **in memory only** — no database, no external services.

- Functional spec: [SPECS.md](SPECS.md)
- Design (SDD): [SDD/](SDD) — architecture, domain model, value objects, services, ports, adapters
- Implementation log: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)

## Run

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Test

```bash
./mvnw test
```

59 tests: value objects, trilateration, message reconstruction, every use case, the in-memory adapter
(including concurrency) and an end-to-end MockMvc suite for F1–F6.

## Endpoints

| Req | Method | Path |
|-----|--------|------|
| F1 Full processing | POST | `/api/interceptions` |
| F2 Register reading | POST | `/api/devices/{deviceName}/readings` |
| F3 List readings | GET | `/api/devices/readings` |
| F3 Get reading | GET | `/api/devices/{deviceName}/readings` |
| F3 Accumulated processing | GET | `/api/interceptions/accumulated` |
| F4 Replace reading | PUT | `/api/devices/{deviceName}/readings` |
| F5 Partial update | PATCH | `/api/devices/{deviceName}/readings` |
| F6 Delete reading | DELETE | `/api/devices/{deviceName}/readings` |

`deviceName`: `star-labs`, `ccpd`, `star-satellite` (display names such as `S.T.A.R. Satellite` are also
accepted). Status codes: 400 invalid data, 404 unknown device / no reading, 409 already registered,
422 insufficient data or location/message cannot be determined. Full contract:
[SDD/adapters/REST Adapter.md](SDD/adapters/REST%20Adapter.md).

### Example (F1)

```bash
curl -X POST http://localhost:8080/api/interceptions -H "Content-Type: application/json" -d '{
  "devices": [
    { "name": "star-labs",      "distance": 485.41, "message": ["savitar", "", "", "the", "lab"] },
    { "name": "ccpd",           "distance": 265.75, "message": ["", "returns", "", "the", ""] },
    { "name": "star-satellite", "distance": 600.52, "message": ["", "", "returns", "at", "", ""] }
  ]
}'
```

```json
{ "success": true, "message": "Interception processed successfully",
  "data": { "position": { "x": -100.0, "y": 75.0 }, "message": "savitar returns at the lab" } }
```

## Postman

Import `postman/OperationAlchemy_API.postman_collection.json` and
`postman/OperationAlchemy_Local.postman_environment.json`. Run the whole collection in the Collection
Runner: it clears the storage first and then exercises F1–F6 plus error cases (45 requests with
automated tests).
