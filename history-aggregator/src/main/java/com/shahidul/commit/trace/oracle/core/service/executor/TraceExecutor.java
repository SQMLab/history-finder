package com.shahidul.commit.trace.oracle.core.service.executor;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;

/**
 * @author Shahidul Islam
 * @since 11/12/2023
 */
public interface TraceExecutor {
    void execute();
    TraceEntity execute(TraceEntity traceEntity, TraceService traceService);
}
