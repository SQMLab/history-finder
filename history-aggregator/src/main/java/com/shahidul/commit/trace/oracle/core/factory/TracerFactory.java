package com.shahidul.commit.trace.oracle.core.factory;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;

/**
 * @since 2/6/2024
 */
public interface TracerFactory {
    TraceService findTraceService(TracerName tracerName);

}
