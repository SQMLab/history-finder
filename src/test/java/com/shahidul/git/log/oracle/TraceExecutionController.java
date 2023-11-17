package com.shahidul.git.log.oracle;

import com.shahidul.git.log.oracle.core.service.loader.DataSetLoader;
import com.shahidul.git.log.oracle.core.service.aggregator.TraceAggregatorService;
import com.shahidul.git.log.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.git.log.oracle.core.service.executor.TraceExecutor;
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

/*    @Test
    @Order(-1)
    public void preProcessCodeShovelTest() {
        dataSetLoader.preProcessCodeShoveFile();

    }*/

    @Test
    @Order(0)
    public void loadDataSet() {
        dataSetLoader.loadFile();

    }
    @Test
    @Order(1)
    void executeTrace() {
        traceExecutor.execute();
    }

    @Test
    @Order(2)
    void aggregate(){
        aggregatorService.aggregate();
    }

    @Test
    @Order(3)
    void runAnalysis(){
        traceAnalyzer.analyze();
    }
}
