package application.domain.exceptions;

/**
 * The distances do not define a single consistent point.
 */
public class LocationUndeterminableException extends UnprocessableInterceptionException {

    public LocationUndeterminableException(String message) {
        super(message);
    }
}
