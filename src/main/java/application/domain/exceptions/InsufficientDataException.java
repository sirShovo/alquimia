package application.domain.exceptions;

import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The accumulated readings are not enough to process the interception.
 */
public class InsufficientDataException extends UnprocessableInterceptionException {

    private final List<SurveillanceDevice> missingDevices;

    public InsufficientDataException(List<SurveillanceDevice> missingDevices) {
        super("Not enough information to determine the location and message. Missing readings from: "
                + missingDevices.stream().map(SurveillanceDevice::getCode).collect(Collectors.joining(", "))
                + ".");
        this.missingDevices = List.copyOf(missingDevices);
    }

    public List<SurveillanceDevice> getMissingDevices() {
        return missingDevices;
    }
}
