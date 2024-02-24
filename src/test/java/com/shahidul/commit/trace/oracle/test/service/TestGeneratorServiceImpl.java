package com.shahidul.commit.trace.oracle.test.service;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.factory.TracerFactory;
import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.loader.DataSetLoader;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 2/7/2024
 */
@Service
@AllArgsConstructor
public class TestGeneratorServiceImpl implements TestGeneratorService {

    @Autowired
    TraceExecutor traceExecutor;

    @Autowired
    DataSetLoader dataSetLoader;

    @Autowired
    MetadataResolverService metadataResolverService;

    @Autowired
    TraceAnalyzer traceAnalyzer;

    @Autowired
    InfluxDbManager influxDbManager;

    @Autowired
    AppProperty appProperty;

    @Autowired
    TracerFactory tracerFactory;

    @Override
    public Stream<DynamicNode> prepareTest(List<TraceEntity> traceEntityList, List<TraceService> traceServiceList) {
        return traceEntityList.stream()
                .map(traceEntity -> DynamicContainer.dynamicContainer(traceEntity.getOracleFileName(),
                        Stream.of(executeAlgorithms(traceEntity, traceServiceList),
                                metaDataOps(traceEntity),
                                analysisOps(traceEntity),
                                timeSeriesOps(traceEntity))
                ));
    }

    @Override
    public DynamicNode executeAlgorithms(TraceEntity traceEntity, List<TraceService> traceServiceList) {
        return DynamicContainer.dynamicContainer("Algorithms",
                traceServiceList.stream()
                        .map(tracerService -> executeAlgorithm(traceEntity, tracerService)));
    }

    @Override
    public DynamicNode metaDataOps(TraceEntity traceEntity) {
        return DynamicTest.dynamicTest("Inject metadata", () -> metadataResolverService.populateMetaData(traceEntity));
    }

    @Override
    public DynamicNode analysisOps(TraceEntity traceEntity) {
        return DynamicTest.dynamicTest("Analysis", () -> traceAnalyzer.analyze(traceEntity));
    }

    @Override
    public DynamicNode timeSeriesOps(TraceEntity traceEntity) {
        return DynamicContainer.dynamicContainer("Times Series", Stream.of(
                deleteTimesSeriesOps(traceEntity),
                insertIntoTimeSeriesOps(traceEntity)));
    }

    @Override
    public DynamicNode deleteTimesSeriesOps(TraceEntity traceEntity) {
        return DynamicContainer.dynamicContainer("Delete Times Series", Stream.of(
                DynamicTest.dynamicTest("Delete Commit Series", () -> influxDbManager.deleteCommitSeriesByFileId(traceEntity.getOracleFileId())),
                DynamicTest.dynamicTest("Delete Analysis Series", () -> influxDbManager.deleteAnalysisSeriesByFileId(traceEntity.getOracleFileId()))));
    }

    @Override
    public DynamicNode insertIntoTimeSeriesOps(TraceEntity traceEntity) {
        return DynamicTest.dynamicTest("Updating Time series", () -> influxDbManager.load(traceEntity));
    }


    private DynamicTest executeAlgorithm(TraceEntity traceEntity, TraceService traceService) {
        return DynamicTest.dynamicTest(traceService.getTracerName(), () -> traceExecutor.execute(traceEntity, traceService));
    }
}
