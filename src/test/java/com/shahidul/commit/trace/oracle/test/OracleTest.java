package com.shahidul.commit.trace.oracle.test;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.factory.TracerFactory;
import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.loader.DataSetLoader;
import com.shahidul.commit.trace.oracle.test.service.TestGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.shahidul.commit.trace.oracle.core.enums.TracerName.DEFAULT_EXECUTION_SEQUENCE;

/**
 * @author Shahidul Islam
 * @since 2/6/2024
 */
@SpringBootTest
@Slf4j
public class OracleTest {
    @Autowired
    TraceExecutor traceExecutor;

    @Autowired
    DataSetLoader dataSetLoader;

    @Autowired
    MetadataResolverService aggregatorService;

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

    @Autowired
    TestGeneratorService testGeneratorService;

    @TestFactory
    Stream<DynamicNode> executeTest() {

        String oracleFileId = environment.getProperty("oracle.file-id", "001");
        List<TracerName> tracerList = Arrays.stream(environment.getProperty("tracer-name", "historyFinder").split(","))
                .filter(code-> !code.isBlank())
                .map(TracerName::fromCode)
                .sorted(Comparator.comparingInt(DEFAULT_EXECUTION_SEQUENCE::indexOf))
                .collect(Collectors.toCollection(ArrayList::new));
        if (tracerList.isEmpty()){
            tracerList.addAll(DEFAULT_EXECUTION_SEQUENCE);
        }
        if (!tracerList.contains(TracerName.AGGREGATED)){
            tracerList.add(TracerName.AGGREGATED);
        }

        TraceEntity traceEntityList = dataSetLoader.loadOracleFile(Integer.parseInt(oracleFileId));

        return testGeneratorService.prepareTest(Arrays.asList(traceEntityList), pickAlgorithms(tracerList));
    }

    private List<TraceService> pickAlgorithms(List<TracerName> tracerNameList){
        Map<String, TraceService> traceServiceMap = traceServiceList.stream()
                .collect(Collectors.toMap(tracerService -> tracerService.getTracerName(), tracerService -> tracerService));
        return tracerNameList.stream()
                .map(tracerName -> traceServiceMap.get(tracerName.getCode()))
                .toList();
    }

}
