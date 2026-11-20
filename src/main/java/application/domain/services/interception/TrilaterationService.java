package application.domain.services.interception;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.LocationUndeterminableException;
import application.domain.valueobjects.Coordinates;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Locates the emitter from the distances reported by the three surveillance devices.
 *
 * <p>Subtracting the circle equations pairwise produces a 2x2 linear system solved with Cramer's
 * rule; the solution is then checked against the three distances.
 */
@Service
public class TrilaterationService {

    /** Maximum accepted difference between a reported distance and the computed one. */
    static final double TOLERANCE = 1.0;
    private static final double EPSILON = 1e-9;
    private static final int OUTPUT_DECIMALS = 2;

    public Coordinates locate(Map<SurveillanceDevice, Distance> distances) {
        if (distances == null) {
            throw new DomainException("Distances are required.");
        }
        for (SurveillanceDevice device : SurveillanceDevice.values()) {
            if (distances.get(device) == null) {
                throw new DomainException("Distance from device '" + device.getCode() + "' is required.");
            }
        }

        Coordinates p1 = SurveillanceDevice.STAR_LABS.getPosition();
        Coordinates p2 = SurveillanceDevice.CCPD.getPosition();
        Coordinates p3 = SurveillanceDevice.STAR_SATELLITE.getPosition();
        double d1 = distances.get(SurveillanceDevice.STAR_LABS).value();
        double d2 = distances.get(SurveillanceDevice.CCPD).value();
        double d3 = distances.get(SurveillanceDevice.STAR_SATELLITE).value();

        double a = 2 * (p2.x() - p1.x());
        double b = 2 * (p2.y() - p1.y());
        double c = d1 * d1 - d2 * d2 - p1.x() * p1.x() + p2.x() * p2.x() - p1.y() * p1.y() + p2.y() * p2.y();
        double d = 2 * (p3.x() - p2.x());
        double e = 2 * (p3.y() - p2.y());
        double f = d2 * d2 - d3 * d3 - p2.x() * p2.x() + p3.x() * p3.x() - p2.y() * p2.y() + p3.y() * p3.y();

        double determinant = a * e - b * d;
        if (Math.abs(determinant) < EPSILON) {
            throw new LocationUndeterminableException(
                    "The location cannot be determined: the devices are collinear.");
        }

        Coordinates location = new Coordinates((c * e - f * b) / determinant, (a * f - d * c) / determinant);

        for (SurveillanceDevice device : SurveillanceDevice.values()) {
            double expected = distances.get(device).value();
            double actual = location.distanceTo(device.getPosition());
            if (Math.abs(actual - expected) > TOLERANCE) {
                throw new LocationUndeterminableException(
                        "The location cannot be determined: the reported distances are inconsistent "
                                + "(they do not intersect at a single point).");
            }
        }
        return location.rounded(OUTPUT_DECIMALS);
    }
}
