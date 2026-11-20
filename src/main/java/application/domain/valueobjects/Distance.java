package application.domain.valueobjects;

import application.domain.exceptions.DomainException;

/**
 * Distance reported by a device to the lab. Must be finite and non-negative.
 */
public record Distance(double value) {

    public Distance {
        if (!Double.isFinite(value)) {
            throw new DomainException("Distance must be a finite number.");
        }
        if (value < 0) {
            throw new DomainException("Distance must be greater than or equal to zero.");
        }
    }
}
