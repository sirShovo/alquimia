package application.domain.ports.in;

import application.domain.models.DeviceReading;
import application.domain.valueobjects.SurveillanceDevice;
import java.util.List;

/** F3 - queries the registered readings. */
public interface ConsultDeviceReadingsUseCase {

    List<DeviceReading> consultAll();

    DeviceReading consult(SurveillanceDevice device);
}
