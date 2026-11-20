package application.domain.ports.in;

import application.domain.models.DeviceReading;

/** F2 - registers the reading of a single device. */
public interface RegisterDeviceReadingUseCase {

    DeviceReading register(DeviceReading reading);
}
