package rnd.method.history.commit.trace.oracle.core.factory;

import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.core.service.algorithm.TraceService;

/**
 * @since 2/6/2024
 */
public interface TracerFactory {
    TraceService findTraceService(TracerName tracerName);

}
