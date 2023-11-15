package com.shahidul.git.log.oracle.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.core.model.Commit;
import com.shahidul.git.log.oracle.core.model.Trace;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.springframework.stereotype.Service;

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
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("stubs/java").getFile());
            Map<String, TraceEntity> entityMap = traceRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(TraceEntity::getUid, Function.identity()));

            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(rootFileDir.listFiles())
                    .map(file -> {
                        try {
                            Trace trace = objectMapper.readValue(file, Trace.class);
                            String uid = generateUid(trace);
                            if (entityMap.containsKey(uid)) {
                                return entityMap.get(uid);
                            } else {
                                return TraceEntity.builder()
                                        .uid(uid)
                                        .repositoryName(trace.getRepositoryName())
                                        .inputLabel(file.getName())
                                        .repositoryUrl(trace.getRepositoryUrl())
                                        .commitHash(trace.getCommitHash())
                                        .filePath(trace.getFilePath())
                                        .filePathAndLine(trace.getFilePath() + ":" + trace.getStartLine())
                                        .elementType(trace.getElementType())
                                        .elementName(trace.getElementName())
                                        .startLine(trace.getStartLine())
                                        .expectedCommits(
                                                trace.getExpectedCommits().stream().map(commit -> CommitEntity.builder()
                                                                .commitHash(commit.getCommitHash())
                                                                .changeType(commit.getChangeType())
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

    @Override
    public void codeShovelToUniformFormat() {
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("stubs/input").getFile());


            //rootFileDir = classPathResource.getFile();
            List<Trace> traceEntityList = Arrays.stream(rootFileDir.listFiles())
                    .map(file -> {
                        try {
                            JsonNode json = objectMapper.readValue(file, JsonNode.class);

                            String repositoryName = json.get("repositoryName").asText();

                            List<Commit> commits = new ArrayList<>();
                            json.get("expectedResult")
                                    .fields()
                                    .forEachRemaining(commit -> {
                                        commits.add(Commit.builder().commitHash(commit.getKey()).changeType(commit.getValue().asText()).build());
                                    });

                            Trace trace = Trace.builder()
                                    .repositoryName(repositoryName)
                                    .repositoryUrl("https://" + repositoryName + "/" + repositoryName)
                                    .commitHash(json.get("startCommitName").asText())
                                    .filePath(json.get("filePath").asText())
                                    .elementType("method")
                                    .elementName(json.get("functionName").asText())
                                    .startLine(json.get("functionStartLine").asInt())
                                    .endLine(null)
                                    .expectedCommits(commits)
                                    .build();
                            File outputFile = new File( "./src/main/resources/stubs/java", file.getName());
                            outputFile.createNewFile();

                            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, trace);
                            objectMapper.readValue(file, Trace.class);
                            return trace;

                        } catch (Exception e) {
                            log.error("File name {}", file.getName());
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private String generateUid(Trace trace) {
        String text = new StringBuilder()
                .append(trace.getRepositoryName())
                .append(trace.getRepositoryUrl())
                .append(trace.getCommitHash())
                .append(trace.getFilePath())
                .append(trace.getElementType())
                .append(trace.getElementName())
                .append(trace.getStartLine())
                .toString();
        return DatatypeConverter.printHexBinary(DIGESTER.digest(text.getBytes(StandardCharsets.UTF_8)));

    }
}
