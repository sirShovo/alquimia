package application.domain.services.interception;

import application.domain.models.DeviceReading;
import application.domain.models.InterceptionResult;
import application.domain.valueobjects.Coordinates;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Combines trilateration and message reconstruction over one complete set of readings
 * (one per device). Shared by F1 and F3 so both produce identical results.
 */
@Service
@RequiredArgsConstructor
public class InterceptionCalculationService {

    private final TrilaterationService trilaterationService;
    private final MessageReconstructionService messageReconstructionService;

    /**
     * @param readings one reading per device; completeness is validated by the caller
     */
    public InterceptionResult calculate(Map<SurveillanceDevice, DeviceReading> readings) {
        Map<SurveillanceDevice, Distance> distances = new EnumMap<>(SurveillanceDevice.class);
        List<MessageFragment> fragments = new ArrayList<>();
        for (SurveillanceDevice device : SurveillanceDevice.values()) {
            DeviceReading reading = readings.get(device);
            distances.put(device, reading.distance());
            fragments.add(reading.fragment());
        }
        Coordinates position = trilaterationService.locate(distances);
        String message = messageReconstructionService.reconstruct(fragments);
        return new InterceptionResult(position, message);
    }
}
