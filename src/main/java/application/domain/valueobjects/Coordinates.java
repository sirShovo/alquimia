package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A point on the Central City plane.
 */
public record Coordinates(double x, double y) {

    public Coordinates {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new DomainException("Coordinates must be finite numbers.");
        }
    }

    /** Euclidean distance to another point. */
    public double distanceTo(Coordinates other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    /** Returns a copy rounded HALF_UP to the given number of decimals. */
    public Coordinates rounded(int decimals) {
        return new Coordinates(round(x, decimals), round(y, decimals));
    }

    private static double round(double value, int decimals) {
        double result = BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
        return result == 0.0 ? 0.0 : result; // normalizes -0.0
    }
}
