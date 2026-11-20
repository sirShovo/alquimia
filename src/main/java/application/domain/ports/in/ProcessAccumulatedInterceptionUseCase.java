package application.domain.ports.in;

import application.domain.models.InterceptionResult;

/** F3 - processes the accumulated readings when there is enough information. */
public interface ProcessAccumulatedInterceptionUseCase {

    InterceptionResult process();
}
