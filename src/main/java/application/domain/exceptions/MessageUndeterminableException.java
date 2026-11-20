package application.domain.exceptions;

/**
 * The fragments do not allow rebuilding the original message (gaps or conflicting words).
 */
public class MessageUndeterminableException extends UnprocessableInterceptionException {

    public MessageUndeterminableException(String message) {
        super(message);
    }
}
