package com.shahidul.commit.trace.oracle.test;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.factory.TracerFactory;
import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.loader.DataSetLoader;
import com.shahidul.commit.trace.oracle.test.service.TestGeneratorService;
import com.shahidul.commit.trace.oracle.util.Util;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @Autowired
    TraceDao traceDao;


    @Order(0)
    @TestFactory
    public DynamicTest loadData() {
        return DynamicTest.dynamicTest("Load Data", ()-> {
            dataSetLoader.loadFile(Integer.MAX_VALUE);
        });
    }

 /*   @TestFactory
    public DynamicTest cleanInfluxDb() {
        return DynamicTest.dynamicTest("Clean InfluxDB", ()-> {
            influxDbManager.deleteAll();
        });
    }*/

    @Order(1)
    @TestFactory
    Stream<DynamicNode> executeTest() {

        String forceCompute = environment.getProperty("run-config.force-compute", "False");

        String oracleFileIdsText = environment.getProperty("run-config.oracle-file-ids", "1");
        List<Integer> oracleFileIdList = Util.parseOraceFileIds(oracleFileIdsText);
        List<TraceEntity> traceEntityList = traceDao.findByOracleFileIdList(oracleFileIdList);
        List<TracerName> tracerList = Arrays.stream(environment.getProperty("run-config.tracer-name", "historyFinder").split(","))
                .filter(code -> !code.isBlank())
                .map(TracerName::fromCode)
                .sorted(Comparator.comparingInt(DEFAULT_EXECUTION_SEQUENCE::indexOf))
                .collect(Collectors.toCollection(ArrayList::new));
        if (tracerList.isEmpty()) {
            tracerList.addAll(DEFAULT_EXECUTION_SEQUENCE);
        }
        if (!tracerList.contains(TracerName.AGGREGATED)) {
            tracerList.add(TracerName.AGGREGATED);
        }
        return testGeneratorService.prepareTest(traceEntityList, pickAlgorithms(tracerList), Boolean.valueOf(forceCompute));
    }

    private List<TraceService> pickAlgorithms(List<TracerName> tracerNameList) {
        Map<String, TraceService> traceServiceMap = traceServiceList.stream()
                .collect(Collectors.toMap(tracerService -> tracerService.getTracerName(), tracerService -> tracerService));
        return tracerNameList.stream()
                .map(tracerName -> traceServiceMap.get(tracerName.getCode()))
                .toList();
    }

}
