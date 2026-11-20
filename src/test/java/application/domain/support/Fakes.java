package application.domain.support;

import application.domain.models.DeviceReading;
import application.domain.ports.out.DeviceReadingRepositoryPort;
import application.domain.services.interception.InterceptionCalculationService;
import application.domain.services.interception.MessageReconstructionService;
import application.domain.services.interception.TrilaterationService;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Test doubles and sample data shared by the domain tests.
 *
 * <p>Sample scenario: lab at (-100, 75) and message "savitar returns at the lab".
 */
public final class Fakes {

    public static final String EXPECTED_MESSAGE = "savitar returns at the lab";

    private Fakes() {
    }

    public static DeviceReading starLabs() {
        return new DeviceReading(SurveillanceDevice.STAR_LABS, new Distance(485.41),
                new MessageFragment(List.of("savitar", "", "", "the", "lab")));
    }

    public static DeviceReading ccpd() {
        return new DeviceReading(SurveillanceDevice.CCPD, new Distance(265.75),
                new MessageFragment(List.of("", "returns", "", "the", "")));
    }

    public static DeviceReading starSatellite() {
        return new DeviceReading(SurveillanceDevice.STAR_SATELLITE, new Distance(600.52),
                new MessageFragment(List.of("", "", "returns", "at", "", "")));
    }

    public static List<DeviceReading> allReadings() {
        return List.of(starLabs(), ccpd(), starSatellite());
    }

    public static InterceptionCalculationService calculationService() {
        return new InterceptionCalculationService(new TrilaterationService(), new MessageReconstructionService());
    }

    /** Simple map-based implementation of the output port. */
    public static class FakeDeviceReadingRepository implements DeviceReadingRepositoryPort {

        private final Map<SurveillanceDevice, DeviceReading> store = new EnumMap<>(SurveillanceDevice.class);

        @Override
        public List<DeviceReading> findAll() {
            return store.values().stream().sorted(Comparator.comparing(DeviceReading::device)).toList();
        }

        @Override
        public Optional<DeviceReading> findBy(SurveillanceDevice device) {
            return Optional.ofNullable(store.get(device));
        }

        @Override
        public boolean saveIfAbsent(DeviceReading reading) {
            return store.putIfAbsent(reading.device(), reading) == null;
        }

        @Override
        public Optional<DeviceReading> replaceIfPresent(DeviceReading reading) {
            return Optional.ofNullable(store.computeIfPresent(reading.device(), (k, v) -> reading));
        }

        @Override
        public Optional<DeviceReading> updateIfPresent(SurveillanceDevice device, UnaryOperator<DeviceReading> updater) {
            return Optional.ofNullable(store.computeIfPresent(device, (k, v) -> updater.apply(v)));
        }

        @Override
        public boolean deleteIfPresent(SurveillanceDevice device) {
            return store.remove(device) != null;
        }
    }
}
