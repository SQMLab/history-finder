package rnd.method.history.commit.trace.oracle.core.service.executor;

import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.method.history.commit.trace.oracle.core.service.algorithm.TraceService;

/**
 * @since 11/12/2023
 */
public interface TraceExecutor {
    void execute();
    TraceEntity execute(TraceEntity traceEntity, TraceService traceService);
}
