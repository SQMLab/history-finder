package com.shahidul.git.log.oracle.core.service;

import com.shahidul.git.log.oracle.core.model.LogTracerInput;
import com.shahidul.git.log.oracle.core.model.LogTracerOutput;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
public interface GitTracer {
    LogTracerOutput trace(LogTracerInput tracerInput);
}
