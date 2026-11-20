package application.domain.models;

import application.domain.exceptions.DomainException;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;

/**
 * Aggregate root: the information a surveillance device reported about the lab.
 * There is at most one reading per device; the device is the aggregate identity.
 */
public record DeviceReading(SurveillanceDevice device, Distance distance, MessageFragment fragment) {

    public DeviceReading {
        if (device == null) {
            throw new DomainException("Device is required.");
        }
        if (distance == null) {
            throw new DomainException("Distance is required.");
        }
        if (fragment == null) {
            throw new DomainException("Message fragment is required.");
        }
    }

    public DeviceReading withDistance(Distance newDistance) {
        return new DeviceReading(device, newDistance, fragment);
    }

    public DeviceReading withFragment(MessageFragment newFragment) {
        return new DeviceReading(device, distance, newFragment);
    }
}
