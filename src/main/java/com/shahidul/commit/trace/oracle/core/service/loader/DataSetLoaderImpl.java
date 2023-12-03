package com.shahidul.commit.trace.oracle.core.service.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.AlgorithmExecution;
import com.shahidul.commit.trace.oracle.core.model.Commit;
import com.shahidul.commit.trace.oracle.core.model.Trace;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AlgorithmExecutionUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    static final Map<String, String> repoMap = new HashMap<>();


    static {
        try {
            DIGESTER = MessageDigest.getInstance("SHA-256");
            repoMap.put("checkstyle", "https://github.com/checkstyle/checkstyle.git");
            repoMap.put("commons-lang", "https://github.com/apache/commons-lang.git");
            repoMap.put("flink", "https://github.com/apache/flink.git");
            repoMap.put("hibernate-orm", "https://github.com/hibernate/hibernate-orm.git");
            repoMap.put("javaparser", "https://github.com/javaparser/javaparser.git");
            repoMap.put("jgit", "https://gerrit.googlesource.com/jgit");
            repoMap.put("junit4", "https://github.com/junit-team/junit4.git");
            repoMap.put("junit5", "https://github.com/junit-team/junit5.git");
            repoMap.put("okhttp", "https://github.com/square/okhttp.git");
            repoMap.put("spring-framework", "https://github.com/spring-projects/spring-framework.git");
            repoMap.put("commons-io", "https://github.com/apache/commons-io.git");
            repoMap.put("elasticsearch", "https://github.com/elastic/elasticsearch.git");
            repoMap.put("hadoop", "https://github.com/apache/hadoop.git");
            repoMap.put("hibernate-search", "https://github.com/hibernate/hibernate-search.git");
            repoMap.put("intellij-community", "https://github.com/JetBrains/intellij-community.git");
            repoMap.put("jetty.project", "https://github.com/eclipse/jetty.project.git");
            repoMap.put("lucene-solr", "https://github.com/apache/lucene-solr.git");
            repoMap.put("mockito", "https://github.com/mockito/mockito.git");
            repoMap.put("pmd", "https://github.com/pmd/pmd.git");
            repoMap.put("spring-boot", "https://github.com/spring-projects/spring-boot.git");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    @PostConstruct
    public void init() {
    }

    @Override
    //@PostConstruct
    public void loadFile(int limit) {
        log.info("loading data set ..");
        //ClassPathResource classPathResource = new ClassPathResource("classpath:oracle/method/training", MethodTracker.class.getClassLoader());
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("stubs/java").getFile());
            Map<String, TraceEntity> entityMap = traceRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(TraceEntity::getOracleFileName, Function.identity()));

            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(rootFileDir.listFiles())
                    .limit(limit)
                    .map(file -> {
                        try {
                            Trace trace = objectMapper.readValue(file, Trace.class);
                            String oracleFileName = file.getName();
                            if (entityMap.containsKey(oracleFileName)) {
                                return entityMap.get(oracleFileName);
                            } else {
                                HashMap<String, AlgorithmExecutionUdt> analysisEntityMap = new HashMap<>();
                                for(Map.Entry<String, AlgorithmExecution> entry : trace.getAnalysis().entrySet()){

                                    List<CommitUdt> commitList = entry.getValue().getCommits()
                                            .stream()
                                            .map(commit -> CommitUdt.builder().tracerName(entry.getKey()).commitHash(commit.getCommitHash()).changeType(commit.getChangeType()).build())
                                            .toList();
                                    analysisEntityMap.put(entry.getKey(), AlgorithmExecutionUdt.builder().commits(commitList).build());
                                }
                                String uid = generateUid(trace);
                                return TraceEntity.builder()
                                        .uid(uid)
                                        .oracleFileId(Integer.valueOf(oracleFileName.substring(0, 3)))
                                        .oracleFileName(oracleFileName)
                                        .repositoryName(trace.getRepositoryName())
                                        .repositoryUrl(trace.getRepositoryUrl())
                                        .startCommitHash(trace.getCommitHash())
                                        .filePath(trace.getFilePath())
                                        .elementType(trace.getElementType())
                                        .elementName(trace.getElementName())
                                        .startLine(trace.getStartLine())
                                        .endLine(trace.getEndLine())
                                        .expectedCommits(
                                                trace.getExpectedCommits().stream().map(commit -> CommitUdt.builder()
                                                                .tracerName(TracerName.EXPECTED.getCode())
                                                                .commitHash(commit.getCommitHash())
                                                                .changeType(commit.getChangeType())
                                                                .build())
                                                        .toList()
                                        )
                                        .analysis(analysisEntityMap)
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
    public void preProcessCodeShoveFile() {
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("stubs/input").getFile());


            AtomicInteger fileNo = new AtomicInteger(0);
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
                            List<Commit> ideaCommits = Arrays.stream(json.get("intelliJ").asText().split(" "))
                                    .sorted(Comparator.reverseOrder())
                                    .map(hash -> Commit.builder().commitHash(hash).build()).toList();

                            HashMap<String, AlgorithmExecution> analysis = new HashMap<>();
                            analysis.put(TracerName.INTELLI_J.getCode(), AlgorithmExecution.builder().commits(ideaCommits).build());
                            Trace trace = Trace.builder()
                                    .repositoryName(repositoryName)
                                    .repositoryUrl(repoMap.get(repositoryName))
                                    .commitHash(json.get("startCommitName").asText())
                                    .filePath(json.get("filePath").asText())
                                    .elementType("method")
                                    .elementName(json.get("functionName").asText())
                                    .startLine(json.get("functionStartLine").asInt())
                                    .endLine(json.get("functionEndLine").asInt())
                                    .expectedCommits(commits)
                                    .analysis(analysis)
                                    .build();
                            File outputFile = new File( "./src/main/resources/stubs/java", String.format("%03d", fileNo.incrementAndGet()) + "-" + file.getName());
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
                .append(trace.getEndLine())
                .toString();
        return DatatypeConverter.printHexBinary(DIGESTER.digest(text.getBytes(StandardCharsets.UTF_8)));
    }

}
