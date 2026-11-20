package application.domain.exceptions;

import application.domain.valueobjects.SurveillanceDevice;

/**
 * A reading already exists for the device; it must be replaced (PUT) or partially updated (PATCH).
 */
public class DeviceReadingAlreadyRegisteredException extends DomainException {

    public DeviceReadingAlreadyRegisteredException(SurveillanceDevice device) {
        super("Device '" + device.getCode()
                + "' already has a registered reading. Use replace or partial update to change it.");
    }
}
