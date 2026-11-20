package application.domain.exceptions;

import application.domain.valueobjects.SurveillanceDevice;

/**
 * No reading is currently stored for the given device.
 */
public class DeviceReadingNotFoundException extends DomainException {

    public DeviceReadingNotFoundException(SurveillanceDevice device) {
        super("No reading registered for device '" + device.getCode() + "'.");
    }
}
