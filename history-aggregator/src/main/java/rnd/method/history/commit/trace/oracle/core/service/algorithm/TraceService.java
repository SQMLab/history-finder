package rnd.method.history.commit.trace.oracle.core.service.algorithm;

import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.git.history.finder.dto.ChangeTag;

/**
 * @since 11/10/2023
 */
public interface TraceService {
    String getTracerName();
    ChangeTag parseChangeType(String rawChangeType);
    TraceEntity trace(TraceEntity traceEntity);
}
