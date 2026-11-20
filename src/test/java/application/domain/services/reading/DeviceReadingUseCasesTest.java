package application.domain.services.reading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.DeviceReadingAlreadyRegisteredException;
import application.domain.exceptions.DeviceReadingNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.models.DeviceReading;
import application.domain.support.Fakes;
import application.domain.support.Fakes.FakeDeviceReadingRepository;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeviceReadingUseCasesTest {

    private final FakeDeviceReadingRepository repository = new FakeDeviceReadingRepository();
    private final RegisterDeviceReadingService register = new RegisterDeviceReadingService(repository);
    private final ConsultDeviceReadingsService consult = new ConsultDeviceReadingsService(repository);
    private final ReplaceDeviceReadingService replace = new ReplaceDeviceReadingService(repository);
    private final UpdateDeviceReadingPartiallyService update = new UpdateDeviceReadingPartiallyService(repository);
    private final DeleteDeviceReadingService delete = new DeleteDeviceReadingService(repository);

    // ---- F2 ----

    @Test
    void registersReadingsInAnyOrder() {
        register.register(Fakes.starSatellite());
        register.register(Fakes.starLabs());

        assertThat(consult.consultAll()).extracting(DeviceReading::device)
                .containsExactly(SurveillanceDevice.STAR_LABS, SurveillanceDevice.STAR_SATELLITE);
    }

    @Test
    void rejectsRegisteringTheSameDeviceTwice() {
        register.register(Fakes.ccpd());
        assertThatThrownBy(() -> register.register(Fakes.ccpd()))
                .isInstanceOf(DeviceReadingAlreadyRegisteredException.class);
        assertThatThrownBy(() -> register.register(null)).isInstanceOf(DomainException.class);
    }

    // ---- F3 ----

    @Test
    void consultsOneReading() {
        register.register(Fakes.ccpd());
        assertThat(consult.consult(SurveillanceDevice.CCPD)).isEqualTo(Fakes.ccpd());
        assertThatThrownBy(() -> consult.consult(SurveillanceDevice.STAR_LABS))
                .isInstanceOf(DeviceReadingNotFoundException.class);
    }

    // ---- F4 ----

    @Test
    void replacesTheWholeReadingWithoutAffectingOthers() {
        register.register(Fakes.ccpd());
        register.register(Fakes.starLabs());
        DeviceReading replacement = new DeviceReading(SurveillanceDevice.CCPD, new Distance(10),
                new MessageFragment(List.of("new")));

        assertThat(replace.replace(replacement)).isEqualTo(replacement);
        assertThat(consult.consult(SurveillanceDevice.CCPD)).isEqualTo(replacement);
        assertThat(consult.consult(SurveillanceDevice.STAR_LABS)).isEqualTo(Fakes.starLabs());
    }

    @Test
    void replacingAnUnregisteredDeviceFails() {
        assertThatThrownBy(() -> replace.replace(Fakes.ccpd())).isInstanceOf(DeviceReadingNotFoundException.class);
    }

    // ---- F5 ----

    @Test
    void updatesOnlyTheDistance() {
        register.register(Fakes.ccpd());

        DeviceReading updated = update.update(SurveillanceDevice.CCPD, new Distance(300), null);

        assertThat(updated.distance()).isEqualTo(new Distance(300));
        assertThat(updated.fragment()).isEqualTo(Fakes.ccpd().fragment());
    }

    @Test
    void updatesOnlyTheFragment() {
        register.register(Fakes.ccpd());
        MessageFragment fragment = new MessageFragment(List.of("only", "words"));

        DeviceReading updated = update.update(SurveillanceDevice.CCPD, null, fragment);

        assertThat(updated.distance()).isEqualTo(Fakes.ccpd().distance());
        assertThat(updated.fragment()).isEqualTo(fragment);
    }

    @Test
    void updatesBothFields() {
        register.register(Fakes.ccpd());
        MessageFragment fragment = new MessageFragment(List.of("x"));

        DeviceReading updated = update.update(SurveillanceDevice.CCPD, new Distance(1), fragment);

        assertThat(updated).isEqualTo(new DeviceReading(SurveillanceDevice.CCPD, new Distance(1), fragment));
    }

    @Test
    void partialUpdateRequiresAtLeastOneFieldAndAnExistingReading() {
        register.register(Fakes.ccpd());
        assertThatThrownBy(() -> update.update(SurveillanceDevice.CCPD, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("At least one field");
        assertThatThrownBy(() -> update.update(SurveillanceDevice.STAR_LABS, new Distance(1), null))
                .isInstanceOf(DeviceReadingNotFoundException.class);
    }

    // ---- F6 ----

    @Test
    void deletesOnlyTheSelectedDevice() {
        register.register(Fakes.ccpd());
        register.register(Fakes.starLabs());

        delete.delete(SurveillanceDevice.CCPD);

        assertThat(consult.consultAll()).containsExactly(Fakes.starLabs());
        assertThatThrownBy(() -> delete.delete(SurveillanceDevice.CCPD))
                .isInstanceOf(DeviceReadingNotFoundException.class);
    }

    @Test
    void deletedDeviceCanBeRegisteredAgain() {
        register.register(Fakes.ccpd());
        delete.delete(SurveillanceDevice.CCPD);

        assertThat(register.register(Fakes.ccpd())).isEqualTo(Fakes.ccpd());
    }
}
