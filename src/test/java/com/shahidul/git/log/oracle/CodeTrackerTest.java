package com.shahidul.git.log.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.core.mongo.repository.GitLogRepository;
import com.shahidul.git.log.oracle.core.service.GitTracer;
import com.shahidul.git.log.oracle.core.model.LogTracerInput;
import com.shahidul.git.log.oracle.core.model.LogTracerOutput;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class CodeTrackerTest {

    @Autowired
    GitTracer gitTracer;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GitLogRepository gitLogRepository;

    @Test
    void executeTrace() throws JsonProcessingException {
        LogTracerOutput traceOutput = gitTracer.trace(LogTracerInput.builder().gitLogEntityList(gitLogRepository.findAll()).build());
        log.info("Output {}", objectMapper.writeValueAsString(traceOutput.getGitLogList()));
        gitLogRepository.saveAll(traceOutput.getGitLogList());
    }

}
