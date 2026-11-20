package application.domain.models;

import application.domain.valueobjects.Coordinates;

/**
 * Outcome of processing the readings of the three devices: lab location and rebuilt message.
 */
public record InterceptionResult(Coordinates position, String message) {
}
