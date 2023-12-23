package com.shahidul.commit.trace.oracle.test;

import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.service.loader.DataSetLoader;
import com.shahidul.commit.trace.oracle.core.service.aggregator.TraceAggregatorService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraceExecutionController {

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

   /* @Test
    @Order(-2)
    public void cleanMongoDb() {
        dataSetLoader.cleanDb();

    }*/

 /*   @Test
    @Order(-1)
    public void preProcessCodeShovelTest() {
        dataSetLoader.preProcessCodeShoveFile();

    }*/

    @Test
    @Order(0)
    public void loadDataSet() {
        dataSetLoader.loadFile(3);

    }

    @Test
    @Order(1)
    void executeTrace() {
        traceExecutor.execute();
    }

    @Test
    @Order(2)
    void populateCommitMetaData() {
        aggregatorService.populateMetaData();
    }

    @Test
    @Order(3)
    void aggregate() {
        aggregatorService.aggregate();
    }

    @Test
    @Order(3)
    void runAnalysis() {
        traceAnalyzer.analyze();
    }

    @Test
    @Order(4)
    void loadIntoInfluxDb() {
        influxDbManager.load();
    }
}
