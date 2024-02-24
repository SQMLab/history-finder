package com.shahidul.commit.trace.oracle.test.service;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import org.junit.jupiter.api.DynamicNode;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 2/7/2024
 */
public interface TestGeneratorService {
    Stream<DynamicNode> prepareTest(List<TraceEntity> traceEntityList, List<TraceService> traceServiceList);

    DynamicNode executeAlgorithms(TraceEntity traceEntity, List<TraceService> traceServiceList);

    DynamicNode metaDataOps(TraceEntity traceEntity);
    DynamicNode analysisOps(TraceEntity traceEntity);

    DynamicNode timeSeriesOps(TraceEntity traceEntity);

    DynamicNode deleteTimesSeriesOps(TraceEntity traceEntity);

    DynamicNode insertIntoTimeSeriesOps(TraceEntity traceEntity);
}
