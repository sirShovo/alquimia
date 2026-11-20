package application.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.UnknownDeviceException;
import application.domain.models.DeviceReading;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueObjectsTest {

    @ParameterizedTest
    @ValueSource(strings = {"star-satellite", "S.T.A.R. Satellite", "STAR_SATELLITE", "starsatellite", " Star Satellite "})
    void surveillanceDeviceAcceptsLenientNames(String name) {
        assertThat(SurveillanceDevice.fromName(name)).isEqualTo(SurveillanceDevice.STAR_SATELLITE);
    }

    @Test
    void surveillanceDeviceResolvesEveryCode() {
        assertThat(SurveillanceDevice.fromName("star-labs")).isEqualTo(SurveillanceDevice.STAR_LABS);
        assertThat(SurveillanceDevice.fromName("STAR Labs")).isEqualTo(SurveillanceDevice.STAR_LABS);
        assertThat(SurveillanceDevice.fromName("ccpd")).isEqualTo(SurveillanceDevice.CCPD);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "kenobi", "star"})
    void surveillanceDeviceRejectsUnknownNames(String name) {
        assertThatThrownBy(() -> SurveillanceDevice.fromName(name)).isInstanceOf(UnknownDeviceException.class);
    }

    @Test
    void surveillanceDeviceRejectsNull() {
        assertThatThrownBy(() -> SurveillanceDevice.fromName(null)).isInstanceOf(UnknownDeviceException.class);
    }

    @Test
    void surveillanceDevicesHaveTheSpecifiedPositions() {
        assertThat(SurveillanceDevice.STAR_LABS.getPosition()).isEqualTo(new Coordinates(-500, -200));
        assertThat(SurveillanceDevice.CCPD.getPosition()).isEqualTo(new Coordinates(100, -100));
        assertThat(SurveillanceDevice.STAR_SATELLITE.getPosition()).isEqualTo(new Coordinates(500, 100));
    }

    @Test
    void distanceAcceptsZeroAndPositiveValues() {
        assertThat(new Distance(0).value()).isZero();
        assertThat(new Distance(12.5).value()).isEqualTo(12.5);
    }

    @Test
    void distanceRejectsNegativeAndNonFiniteValues() {
        assertThatThrownBy(() -> new Distance(-0.1)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Distance(Double.NaN)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Distance(Double.POSITIVE_INFINITY)).isInstanceOf(DomainException.class);
    }

    @Test
    void messageFragmentTrimsWordsAndIsImmutable() {
        List<String> source = new ArrayList<>(List.of(" savitar ", "", "  "));
        MessageFragment fragment = new MessageFragment(source);
        source.set(0, "changed");

        assertThat(fragment.words()).containsExactly("savitar", "", "");
        assertThat(fragment.isGap(1)).isTrue();
        assertThat(fragment.isGap(2)).isTrue();
        assertThatThrownBy(() -> fragment.words().add("x")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void messageFragmentRejectsNullListOrNullWords() {
        assertThatThrownBy(() -> new MessageFragment(null)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new MessageFragment(Arrays.asList("a", null))).isInstanceOf(DomainException.class);
    }

    @Test
    void coordinatesRoundAndComputeDistance() {
        Coordinates point = new Coordinates(-99.99948, 74.995956);
        assertThat(point.rounded(2)).isEqualTo(new Coordinates(-100.0, 75.0));
        assertThat(new Coordinates(0, 0).distanceTo(new Coordinates(3, 4))).isEqualTo(5.0);
        assertThat(new Coordinates(-0.001, 0).rounded(2).x()).isEqualTo(0.0);
    }

    @Test
    void deviceReadingRequiresEveryComponent() {
        Distance distance = new Distance(1);
        MessageFragment fragment = new MessageFragment(List.of("a"));
        assertThatThrownBy(() -> new DeviceReading(null, distance, fragment)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new DeviceReading(SurveillanceDevice.CCPD, null, fragment))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new DeviceReading(SurveillanceDevice.CCPD, distance, null))
                .isInstanceOf(DomainException.class);
    }
}
