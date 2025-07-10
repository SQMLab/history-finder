package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.git.history.finder.dto.ChangeTag;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
public interface TraceService {
    String getTracerName();
    ChangeTag parseChangeType(String rawChangeType);
    TraceEntity trace(TraceEntity traceEntity);
}
