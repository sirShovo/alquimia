package application.domain.ports.in;

import application.domain.models.DeviceReading;
import application.domain.valueobjects.Distance;
import application.domain.valueobjects.MessageFragment;
import application.domain.valueobjects.SurveillanceDevice;

/**
 * F5 - partially updates the registered reading of a device. {@code null} arguments mean
 * "not provided" and keep their previous value.
 */
public interface UpdateDeviceReadingPartiallyUseCase {

    DeviceReading update(SurveillanceDevice device, Distance distance, MessageFragment fragment);
}
