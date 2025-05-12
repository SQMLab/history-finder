package com.shahidul.commit.trace.oracle.core.service.aggregator;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
public interface MetadataResolverService {
    TraceEntity populateMetaData(TraceEntity traceEntity);
}
