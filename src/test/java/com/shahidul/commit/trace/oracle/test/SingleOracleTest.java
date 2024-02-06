package com.shahidul.commit.trace.oracle.test;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.factory.TracerFactory;
import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.TraceAggregatorService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.loader.DataSetLoader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 2/6/2024
 */
@SpringBootTest
@Slf4j
public class SingleOracleTest {
    @Autowired
    TraceExecutor traceExecutor;

    @Autowired
    DataSetLoader dataSetLoader;

    @Autowired
    TraceAggregatorService aggregatorService;

    @Autowired
    TraceAnalyzer traceAnalyzer;

    @Autowired
    InfluxDbManager influxDbManager;

    @Autowired
    AppProperty appProperty;

    @Autowired
    List<TraceService> traceServiceList;

    @Autowired
    Environment environment;

    @Autowired
    TracerFactory tracerFactory;

    @TestFactory
    Stream<DynamicNode> f() {
        String oracleFileId = environment.getProperty("oracle.file-id", "001");
        List<TracerName> tracerList = Arrays.stream(environment.getProperty("tracer-name", "").split(","))
                .map(TracerName::fromCode).toList();
        if (tracerList.isEmpty()){
            tracerList.addAll(TracerName.IMPLEMENTED);
        }
        TraceEntity traceEntity = dataSetLoader.loadOracleFile(Integer.parseInt(oracleFileId));

        return Stream.of(traceEntity)
                .map(tc -> DynamicContainer.dynamicContainer(tc.getOracleFileName(),
                        tracerList.stream()
                                .map(tracerFactory::findTraceService)
                                .map(tracerService -> createOracleTest(traceEntity, tracerService))));
    }

    @TestFactory
    Stream<DynamicTest> executeAnOracle() {
        String oracleFileId = environment.getProperty("oracle.file-id", "001");
        String tracerName = environment.getProperty("tracer-name");
        List<TracerName> tracerNameList = tracerName != null ? List.of(TracerName.fromCode(tracerName)) : List.of(TracerName.values());
        TraceEntity traceEntity = dataSetLoader.loadOracleFile(Integer.parseInt(oracleFileId));
        return tracerNameList.stream()
                .filter(name -> {
                    try {
                        tracerFactory.findTraceService(name);
                        return true;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .map(tracerFactory::findTraceService)
                .map(tracerService -> createOracleTest(traceEntity, tracerService));
    }

    @TestFactory
    //@DisplayName("Dynamic Test")
    public DynamicTest playTest() {
        log.info("Dtest?");
        return DynamicTest.dynamicTest("Footest", () -> Assertions.assertTrue(true));
    }

    private DynamicTest createOracleTest(TraceEntity traceEntity, TraceService traceService) {
        return DynamicTest.dynamicTest(traceService.getTracerName(), () -> {
            traceExecutor.execute(traceEntity, traceService);
        });
    }
}
