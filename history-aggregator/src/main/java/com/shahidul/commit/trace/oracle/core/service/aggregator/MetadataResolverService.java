package com.shahidul.commit.trace.oracle.core.service.aggregator;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 11/13/2023
 */
public interface MetadataResolverService {
    TraceEntity populateMetaData(TraceEntity traceEntity);
}
