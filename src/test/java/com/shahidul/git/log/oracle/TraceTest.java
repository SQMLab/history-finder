package com.shahidul.git.log.oracle;

import com.shahidul.git.log.oracle.core.service.DataSetLoader;
import com.shahidul.git.log.oracle.core.service.TraceExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class TraceTest {

    @Autowired
    TraceExecutor traceExecutor;

    @Autowired
    DataSetLoader dataSetLoader;

    @Test
    public void loadDataSet() {
        dataSetLoader.load();

    }
    @Test
    void executeTrace() {
        traceExecutor.execute();
    }

}
