# Domain Value Objects

Location: `application.domain.valueobjects`. All value objects are immutable and validate their
invariants on construction, throwing `DomainException` (or a subtype) when violated.

---

## SurveillanceDevice (enum)

Closed catalog of the three surveillance devices and their fixed positions.

| Constant         | Code             | Display name        | Position      |
|------------------|------------------|---------------------|---------------|
| `STAR_LABS`      | `star-labs`      | STAR Labs           | (-500, -200)  |
| `CCPD`           | `ccpd`           | CCPD                | (100, -100)   |
| `STAR_SATELLITE` | `star-satellite` | S.T.A.R. Satellite  | (500, 100)    |

```java
SurveillanceDevice fromName(String name); // lenient lookup
String getCode();
String getDisplayName();
Coordinates getPosition();
```

`fromName` normalizes the input (lower-case, removes every non-alphanumeric char) and compares it with the
normalized code, display name and constant name. `null`/blank or no match → `UnknownDeviceException`.

---

## Coordinates (record)

```java
record Coordinates(double x, double y)
```

- Both components must be finite.
- `distanceTo(Coordinates other)` — Euclidean distance.
- `rounded(int decimals)` — HALF_UP rounding used for API output.

---

## Distance (record)

```java
record Distance(double value)
```

- Must be finite (`NaN`/`Infinity` rejected) and `>= 0`.

---

## MessageFragment (record)

```java
record MessageFragment(List<String> words)
```

- The list must not be `null`; elements must not be `null`.
- `""` (or whitespace-only) means *word not received* — it is a valid gap (BR03).
- Words are stored trimmed; the list is defensively copied and unmodifiable.
- `size()`, `wordAt(int)`, `isGap(int)`.
