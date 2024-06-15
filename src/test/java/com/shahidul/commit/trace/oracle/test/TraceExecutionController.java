package com.shahidul.commit.trace.oracle.test;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.factory.TracerFactory;
import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.loader.DataSetLoader;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraceExecutionController {

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

    private List<TraceEntity> traceEntityList;

    @Autowired
    Environment environment;

    @Autowired
    TraceDao traceDao;

    @Test
    @Order(-2)
    public void cleanMongoDb() {

        //dataSetLoader.cleanDb();
        IntStream.range(1, 500)
                .forEach(id -> {
                    log.info("Deleting ... {}", id);
                    influxDbManager.deleteByFileId(id);

                });

    }

 /*   @Test
    @Order(-1)
    public void preProcessCodeShovelTest() {
        dataSetLoader.preProcessCodeShoveFile();

    }*/

    @Test
    @Order(0)
    public void loadDataSet() {
        this.traceEntityList = dataSetLoader.loadFile(appProperty.getExecutionLimit());
    }

    @TestFactory
    @Order(1)
    Stream<DynamicNode> executeTrace() {
        return traceEntityList.stream()
                .map(traceEntity -> DynamicContainer.dynamicContainer(traceEntity.getOracleFileName(),
                        TracerName.DEFAULT_EXECUTION_SEQUENCE.stream()
                                .map(tracerFactory::findTraceService)
                                .map(tracerService -> createOracleTest(traceEntity, tracerService)))

                );

    }

    @Test
    @Order(2)
    void populateCommitMetaData() {
        //metadataResolverService.populateMetaData();
    }

    @Test
    @Order(3)
    void aggregate() {
        //metadataResolverService.aggregate();
    }

    @Test
    @Order(3)
    void runAnalysis() {
        traceEntityList.stream()
                .map(traceEntity -> traceAnalyzer.analyze(traceEntity))
                .toList();
    }

    @Test
    @Order(4)
    void loadIntoInfluxDb() {
        traceEntityList.stream()
                .map(traceEntity -> influxDbManager.load(traceEntity))
                .toList();
    }


    @Test
    void updateExpectCommits() {
        String oracleFileIdsText = environment.getProperty("run-config.oracle-file-ids", "1");
        List<Integer> oracleFileIdList = Util.parseOracleFileIds(oracleFileIdsText);
        List<TraceEntity> traceEntityList = traceDao.findByOracleFileIdList(oracleFileIdList);
        dataSetLoader.updateExpectedCommit(traceEntityList, TracerName.HISTORY_FINDER);

    }

    private DynamicTest createOracleTest(TraceEntity traceEntity, TraceService traceService) {
        return DynamicTest.dynamicTest(traceService.getTracerName(), () -> {
            traceExecutor.execute(traceEntity, traceService);
        });
    }
}
