package com.shahidul.git.log.oracle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.config.MongoDbConfiguration;
import com.shahidul.git.log.oracle.core.service.DataSetLoader;
import com.shahidul.git.log.oracle.core.service.GitTracer;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.GitCommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
@ContextConfiguration
@Import(MongoDbConfiguration.class)
public class DataSetPreparationTest {
    @Autowired
    DataSetLoader dataSetLoader;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void insertDataSetIntoDb() {
        dataSetLoader.loadDataSet();

    }
}
