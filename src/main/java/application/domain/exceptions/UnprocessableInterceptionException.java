package application.domain.exceptions;

/**
 * Base type for errors where the data is well-formed but the location or message cannot be computed.
 */
public class UnprocessableInterceptionException extends DomainException {

    public UnprocessableInterceptionException(String message) {
        super(message);
    }
}
