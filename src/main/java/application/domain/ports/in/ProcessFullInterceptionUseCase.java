package application.domain.ports.in;

import application.domain.models.DeviceReading;
import application.domain.models.InterceptionResult;
import java.util.List;

/** F1 - processes the readings of the three devices received at once. Nothing is stored. */
public interface ProcessFullInterceptionUseCase {

    InterceptionResult process(List<DeviceReading> readings);
}
