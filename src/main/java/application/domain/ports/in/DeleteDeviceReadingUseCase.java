package application.domain.ports.in;

import application.domain.valueobjects.SurveillanceDevice;

/** F6 - deletes the registered reading of a device. */
public interface DeleteDeviceReadingUseCase {

    void delete(SurveillanceDevice device);
}
