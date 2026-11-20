package application.domain.valueobjects;

import application.domain.exceptions.UnknownDeviceException;
import java.util.Locale;

/**
 * Closed catalog of the surveillance devices deployed in Central City and their fixed positions.
 */
public enum SurveillanceDevice {

    STAR_LABS("star-labs", "STAR Labs", new Coordinates(-500, -200)),
    CCPD("ccpd", "CCPD", new Coordinates(100, -100)),
    STAR_SATELLITE("star-satellite", "S.T.A.R. Satellite", new Coordinates(500, 100));

    private final String code;
    private final String displayName;
    private final Coordinates position;

    SurveillanceDevice(String code, String displayName, Coordinates position) {
        this.code = code;
        this.displayName = displayName;
        this.position = position;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Coordinates getPosition() {
        return position;
    }

    /**
     * Lenient lookup: case-insensitive and ignoring every non-alphanumeric character, so
     * {@code "S.T.A.R. Satellite"}, {@code "star_satellite"} and {@code "star-satellite"} are equivalent.
     *
     * @throws UnknownDeviceException when the name is blank or does not match any device
     */
    public static SurveillanceDevice fromName(String name) {
        String normalized = normalize(name);
        if (normalized.isEmpty()) {
            throw new UnknownDeviceException(name);
        }
        for (SurveillanceDevice device : values()) {
            if (normalized.equals(normalize(device.code))
                    || normalized.equals(normalize(device.displayName))
                    || normalized.equals(normalize(device.name()))) {
                return device;
            }
        }
        throw new UnknownDeviceException(name);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
