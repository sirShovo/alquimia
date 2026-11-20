package application.adapters.in.rest.responses;

/**
 * Lab location and rebuilt message (F1 / F3).
 */
public record InterceptionResponse(CoordinatesResponse position, String message) {
}
