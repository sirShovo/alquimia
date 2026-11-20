# Interception Services

Location: `application.domain.services.interception`.

---

## TrilaterationService

```java
Coordinates locate(Map<SurveillanceDevice, Distance> distances);
```

Requires exactly one distance for each of the three devices (otherwise `DomainException`).

### Algorithm

For each device *i* with position (xᵢ, yᵢ) and distance dᵢ: (x − xᵢ)² + (y − yᵢ)² = dᵢ².

Subtracting circle 2 from circle 1 and circle 3 from circle 2 yields a linear system:

```
A·x + B·y = C        A = 2(x₂ − x₁)  B = 2(y₂ − y₁)  C = d₁² − d₂² − x₁² + x₂² − y₁² + y₂²
D·x + E·y = F        D = 2(x₃ − x₂)  E = 2(y₃ − y₂)  F = d₂² − d₃² − x₂² + x₃² − y₂² + y₃²
det = A·E − B·D
x = (C·E − F·B) / det      y = (A·F − D·C) / det
```

- `|det| < 1e-9` (collinear devices) → `LocationUndeterminableException`.
- **Consistency check:** for every device `|‖P − Pᵢ‖ − dᵢ| ≤ 1.0`; otherwise
  `LocationUndeterminableException` (the three circles do not meet at a single point).
- The result is rounded to 2 decimals.

### Example

Lab at (-100, 75): distances STAR Labs = 485.41, CCPD = 265.75, S.T.A.R. Satellite = 600.52 →
`Coordinates(-100.0, 75.0)`.

---

## MessageReconstructionService

```java
String reconstruct(List<MessageFragment> fragments);
```

### Algorithm

1. At least one fragment is required (`DomainException` otherwise).
2. **Right-align** all fragments to the length *L* of the longest one (shorter fragments are padded with
   leading gaps). The offset at the start represents transmission delay.
3. For every aligned position collect the non-gap words (gaps `""` are ignored — BR03).
   - Two different words → `MessageUndeterminableException` ("conflicting words at position n").
4. Discard the **leading** positions with no word (offset).
5. Any remaining position with no word → `MessageUndeterminableException` ("missing word at position n").
   If nothing remains → `MessageUndeterminableException` ("no words received").
6. Join the words with a single space.

### Example

```
star-labs      : ["savitar", "",        "",   "the", "lab"]
ccpd           : ["",        "returns", "",   "the", ""   ]
star-satellite : ["", "",    "returns", "at", "",    ""   ]   ← one position of offset
result         : "savitar returns at the lab"
```

---

## InterceptionCalculationService

```java
InterceptionResult calculate(Map<SurveillanceDevice, DeviceReading> readings);
```

Takes one reading per device (completeness validated by the caller), calls
`TrilaterationService.locate(...)` with the distances and `MessageReconstructionService.reconstruct(...)`
with the fragments in catalog order. Shared by F1 and F3 so both produce identical results.

---

## ProcessFullInterceptionService (F1)

Implements `ProcessFullInterceptionUseCase`:

```java
InterceptionResult process(List<DeviceReading> readings);
```

1. `readings` must not be null/empty.
2. Duplicated device → `DomainException`.
3. Missing any of the three devices → `DomainException` listing the missing devices (F1 requires the
   three readings at once).
4. `InterceptionCalculationService.calculate(...)`.
5. Returns `InterceptionResult`. **Nothing is stored** — F1 is stateless.

---

## ProcessAccumulatedInterceptionService (F3)

Implements `ProcessAccumulatedInterceptionUseCase`:

```java
InterceptionResult process();
```

1. Reads all stored readings through `DeviceReadingRepositoryPort.findAll()`.
2. If any device is missing → `InsufficientDataException(missingDevices)` ("not enough information").
3. Otherwise calls `InterceptionCalculationService.calculate(...)` (same as F1) and returns the result.
   Stored data is not modified.
