package com.shahidul.git.log.oracle.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.GitCommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.GitLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class DataSetLoaderImpl implements DataSetLoader {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    GitLogRepository gitLogRepository;

    @Override
    //@PostConstruct
    public void loadDataSet() {
        log.info("loading data set ..");
        //ClassPathResource classPathResource = new ClassPathResource("classpath:oracle/method/training", MethodTracker.class.getClassLoader());
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("oracle/method/training").getFile());
            //rootFileDir = classPathResource.getFile();
            List<GitLogEntity> gitLogEntityList = Arrays.stream(rootFileDir.listFiles())
                    .limit(2)
                    .map(file -> {
                        try {
                            GitLog gitLog = objectMapper.readValue(file, GitLog.class);

                            return GitLogEntity.builder()
                                    .repositoryName(gitLog.getRepositoryName())
                                    .repositoryUrl(gitLog.getRepositoryWebURL())
                                    .startCommitId(gitLog.getStartCommitId())
                                    .filePath(gitLog.getFilePath())
                                    .functionName(gitLog.getFunctionName())
                                    .functionKey(gitLog.getFunctionKey())
                                    .startLine(gitLog.getFunctionStartLine())
                                    .expectedCommitList(
                                            gitLog.getExpectedChanges().stream().map(commit -> GitCommitEntity.builder()
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

            gitLogRepository.saveAll(gitLogEntityList);
            log.info("save completed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
