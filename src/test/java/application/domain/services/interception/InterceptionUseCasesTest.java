package application.domain.services.interception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.InsufficientDataException;
import application.domain.models.InterceptionResult;
import application.domain.support.Fakes;
import application.domain.support.Fakes.FakeDeviceReadingRepository;
import application.domain.valueobjects.Coordinates;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import org.junit.jupiter.api.Test;

class InterceptionUseCasesTest {

    private final ProcessFullInterceptionService full = new ProcessFullInterceptionService(Fakes.calculationService());
    private final FakeDeviceReadingRepository repository = new FakeDeviceReadingRepository();
    private final ProcessAccumulatedInterceptionService accumulated =
            new ProcessAccumulatedInterceptionService(repository, Fakes.calculationService());

    // ---- F1 ----

    @Test
    void fullProcessingReturnsLocationAndMessageInAnyOrder() {
        InterceptionResult result = full.process(List.of(Fakes.starSatellite(), Fakes.starLabs(), Fakes.ccpd()));

        assertThat(result.position()).isEqualTo(new Coordinates(-100.0, 75.0));
        assertThat(result.message()).isEqualTo(Fakes.EXPECTED_MESSAGE);
    }

    @Test
    void fullProcessingRequiresTheThreeDevices() {
        assertThatThrownBy(() -> full.process(List.of(Fakes.starLabs(), Fakes.ccpd())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("star-satellite");
        assertThatThrownBy(() -> full.process(List.of())).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> full.process(null)).isInstanceOf(DomainException.class);
    }

    @Test
    void fullProcessingRejectsDuplicatedDevices() {
        assertThatThrownBy(() -> full.process(List.of(Fakes.starLabs(), Fakes.ccpd(), Fakes.ccpd())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("more than once");
    }

    @Test
    void fullProcessingDoesNotStoreAnything() {
        full.process(Fakes.allReadings());
        assertThat(repository.findAll()).isEmpty();
    }

    // ---- F3 ----

    @Test
    void accumulatedProcessingReportsMissingDevices() {
        repository.saveIfAbsent(Fakes.ccpd());

        assertThatThrownBy(accumulated::process)
                .isInstanceOfSatisfying(InsufficientDataException.class, ex -> assertThat(ex.getMissingDevices())
                        .containsExactly(SurveillanceDevice.STAR_LABS, SurveillanceDevice.STAR_SATELLITE));
    }

    @Test
    void accumulatedProcessingWithNoDataReportsEveryDevice() {
        assertThatThrownBy(accumulated::process)
                .isInstanceOfSatisfying(InsufficientDataException.class,
                        ex -> assertThat(ex.getMissingDevices()).hasSize(3));
    }

    @Test
    void accumulatedProcessingUsesTheStoredReadings() {
        Fakes.allReadings().forEach(repository::saveIfAbsent);

        InterceptionResult result = accumulated.process();

        assertThat(result.position()).isEqualTo(new Coordinates(-100.0, 75.0));
        assertThat(result.message()).isEqualTo(Fakes.EXPECTED_MESSAGE);
        assertThat(repository.findAll()).hasSize(3);
    }
}
