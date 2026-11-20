package application.domain.ports.in;

import application.domain.models.DeviceReading;

/** F4 - fully replaces the registered reading of a device. */
public interface ReplaceDeviceReadingUseCase {

    DeviceReading replace(DeviceReading reading);
}
