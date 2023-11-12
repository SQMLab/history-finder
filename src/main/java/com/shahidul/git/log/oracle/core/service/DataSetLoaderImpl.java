package com.shahidul.git.log.oracle.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/11/2023
 */
@Service
@Slf4j
@AllArgsConstructor
public class DataSetLoaderImpl implements DataSetLoader {
    ObjectMapper objectMapper;
    TraceRepository traceRepository;

    @Override
    //@PostConstruct
    public void load() {
        log.info("loading data set ..");
        //ClassPathResource classPathResource = new ClassPathResource("classpath:oracle/method/training", MethodTracker.class.getClassLoader());
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("oracle/method/training").getFile());
            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(rootFileDir.listFiles())
                    .limit(2)
                    .map(file -> {
                        try {
                            GitLog gitLog = objectMapper.readValue(file, GitLog.class);

                            return TraceEntity.builder()
                                    .repositoryName(gitLog.getRepositoryName())
                                    .repositoryUrl(gitLog.getRepositoryWebURL())
                                    .startCommitId(gitLog.getStartCommitId())
                                    .filePath(gitLog.getFilePath())
                                    .functionName(gitLog.getFunctionName())
                                    .functionKey(gitLog.getFunctionKey())
                                    .startLine(gitLog.getFunctionStartLine())
                                    .expectedCommitList(
                                            gitLog.getExpectedChanges().stream().map(commit -> CommitEntity.builder()
                                                            .parentCommitId(commit.getParentCommitId())
                                                            .commitId(commit.getCommitId())
                                                            .commitTime(new Date(commit.getCommitTime()))
                                                            .changeType(commit.getChangeType())
                                                            .elementFileBefore(commit.getElementFileBefore())
                                                            .elementFileAfter(commit.getElementFileAfter())
                                                            .elementNameBefore(commit.getElementNameBefore())
                                                            .elementNameAfter(commit.getElementNameAfter())
                                                            .build())
                                                    .toList()
                                    )
                                    .output(new HashMap<>())
                                    .build();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            traceRepository.saveAll(traceEntityList);
            log.info("save completed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
