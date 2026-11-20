package application.domain.exceptions;

/**
 * The given device name does not match any surveillance device of the catalog.
 */
public class UnknownDeviceException extends DomainException {

    public UnknownDeviceException(String deviceName) {
        super("Unknown surveillance device: '" + deviceName + "'. Valid devices: star-labs, ccpd, star-satellite.");
    }
}
