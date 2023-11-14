package com.shahidul.git.log.oracle.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
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
    private static final MessageDigest DIGESTER;

    static {
        try {
            DIGESTER = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    @PostConstruct
    public void init() {
    }

    @Override
    //@PostConstruct
    public void load() {
        log.info("loading data set ..");
        //ClassPathResource classPathResource = new ClassPathResource("classpath:oracle/method/training", MethodTracker.class.getClassLoader());
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("oracle/method/training").getFile());
            Map<String, TraceEntity> entityMap = traceRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(TraceEntity::getUid, Function.identity()));

            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(rootFileDir.listFiles())
                    .map(file -> {
                        try {
                            GitLog gitLog = objectMapper.readValue(file, GitLog.class);
                            String uid = generateUid(gitLog);
                            if (entityMap.containsKey(uid)) {
                                return entityMap.get(uid);
                            } else {
                                return TraceEntity.builder()
                                        .uid(uid)
                                        .repositoryName(gitLog.getRepositoryName())
                                        .repositoryUrl(gitLog.getRepositoryWebURL())
                                        .commitHash(gitLog.getStartCommitId())
                                        .filePath(gitLog.getFilePath())
                                        .functionName(gitLog.getFunctionName())
                                        .functionKey(gitLog.getFunctionKey())
                                        .startLine(gitLog.getFunctionStartLine())
                                        .expectedCommits(
                                                gitLog.getExpectedChanges().stream().map(commit -> CommitEntity.builder()
                                                                .parentCommitHash(commit.getParentCommitId())
                                                                .commitHash(commit.getCommitId())
                                                                .commitTime(new Date(commit.getCommitTime()))
                                                                .changeType(commit.getChangeType())
                                                                .elementFileBefore(commit.getElementFileBefore())
                                                                .elementFileAfter(commit.getElementFileAfter())
                                                                .elementNameBefore(commit.getElementNameBefore())
                                                                .elementNameAfter(commit.getElementNameAfter())
                                                                .build())
                                                        .toList()
                                        )
                                        .analysis(new HashMap<>())
                                        .build();
                            }
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

    private String generateUid(GitLog gitLog) {
        String text = new StringBuilder()
                .append(gitLog.getRepositoryName())
                .append(gitLog.getRepositoryWebURL())
                .append(gitLog.getStartCommitId())
                .append(gitLog.getFilePath())
                .append(gitLog.getFunctionName())
                .append(gitLog.getFunctionKey())
                .append(gitLog.getFunctionStartLine())
                .toString();
        return DatatypeConverter.printHexBinary(DIGESTER.digest(text.getBytes(StandardCharsets.UTF_8)));

    }
}
