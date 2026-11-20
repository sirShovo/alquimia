package application.domain.services.interception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.LocationUndeterminableException;
import application.domain.valueobjects.Coordinates;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrilaterationServiceTest {

    private final TrilaterationService service = new TrilaterationService();

    @Test
    void locatesTheLabFromRoundedDistances() {
        assertThat(service.locate(distances(485.41, 265.75, 600.52))).isEqualTo(new Coordinates(-100.0, 75.0));
    }

    @Test
    void locatesAnyPointFromExactDistances() {
        Coordinates target = new Coordinates(250, -340);
        Map<SurveillanceDevice, Distance> distances = new EnumMap<>(SurveillanceDevice.class);
        for (SurveillanceDevice device : SurveillanceDevice.values()) {
            distances.put(device, new Distance(target.distanceTo(device.getPosition())));
        }
        assertThat(service.locate(distances)).isEqualTo(target);
    }

    @Test
    void locatesTheLabAtADevicePosition() {
        Coordinates ccpd = SurveillanceDevice.CCPD.getPosition();
        Map<SurveillanceDevice, Distance> distances = new EnumMap<>(SurveillanceDevice.class);
        for (SurveillanceDevice device : SurveillanceDevice.values()) {
            distances.put(device, new Distance(ccpd.distanceTo(device.getPosition())));
        }
        assertThat(service.locate(distances)).isEqualTo(ccpd);
    }

    @Test
    void rejectsInconsistentDistances() {
        assertThatThrownBy(() -> service.locate(distances(100, 100, 100)))
                .isInstanceOf(LocationUndeterminableException.class)
                .hasMessageContaining("inconsistent");
    }

    @Test
    void requiresTheThreeDistances() {
        Map<SurveillanceDevice, Distance> distances = new EnumMap<>(SurveillanceDevice.class);
        distances.put(SurveillanceDevice.STAR_LABS, new Distance(1));
        distances.put(SurveillanceDevice.CCPD, new Distance(1));
        assertThatThrownBy(() -> service.locate(distances))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("star-satellite");
        assertThatThrownBy(() -> service.locate(null)).isInstanceOf(DomainException.class);
    }

    private static Map<SurveillanceDevice, Distance> distances(double starLabs, double ccpd, double satellite) {
        Map<SurveillanceDevice, Distance> distances = new EnumMap<>(SurveillanceDevice.class);
        distances.put(SurveillanceDevice.STAR_LABS, new Distance(starLabs));
        distances.put(SurveillanceDevice.CCPD, new Distance(ccpd));
        distances.put(SurveillanceDevice.STAR_SATELLITE, new Distance(satellite));
        return distances;
    }
}
