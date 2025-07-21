package rnd.method.history.commit.trace.oracle.core.service.aggregator;

import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 11/13/2023
 */
public interface MetadataResolverService {
    TraceEntity populateMetaData(TraceEntity traceEntity);
}
