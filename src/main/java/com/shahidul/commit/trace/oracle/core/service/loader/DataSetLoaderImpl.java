package com.shahidul.commit.trace.oracle.core.service.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.InputTrace;
import com.shahidul.commit.trace.oracle.core.model.InputCommit;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import com.shahidul.commit.trace.oracle.util.Util;
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
    public TraceEntity loadOracleFile(Integer oracleFileId) {
        TraceEntity traceEntity = traceRepository.findByOracleFileId(oracleFileId);
       /* if (traceEntity == null)
        formatOracleFileId(oracleFileId);
        findOracleFiles()
        return null;*/
        return traceEntity;
    }

    @Override
    //@PostConstruct
    public List<TraceEntity> loadFile(int limit) {
        log.info("loading data set ..");
        //ClassPathResource classPathResource = new ClassPathResource("classpath:oracle/method/training", MethodTracker.class.getClassLoader());
        try {
            Map<String, TraceEntity> entityMap = traceRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(TraceEntity::getOracleFileName, Function.identity()));

            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(findOracleFiles())
                    .limit(limit)
                    .map(file -> {
                        try {
                            InputOracle inputOracle = objectMapper.readValue(file, InputOracle.class);
                            String oracleFileName = file.getName();


                            inputOracle.setLanguage("Java");
                            File outputFile = new File("./src/main/resources/oracle", oracleFileName);
                            outputFile.createNewFile();
                            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, inputOracle);


                            if (entityMap.containsKey(oracleFileName)) {
                                return entityMap.get(oracleFileName);
                            } else {
                                String uid = generateUid(inputOracle);
                                return TraceEntity.builder()
                                        .uid(uid)
                                        .oracleFileId(Integer.valueOf(oracleFileName.substring(0, 3)))
                                        .oracleFileName(oracleFileName)
                                        .repositoryName(inputOracle.getRepositoryName())
                                        .repositoryUrl(inputOracle.getRepositoryUrl())
                                        .startCommitHash(inputOracle.getStartCommitHash())
                                        .file(inputOracle.getFile())
                                        .elementType(inputOracle.getElementType())
                                        .elementName(inputOracle.getElement())
                                        .startLine(inputOracle.getStartLine())
                                        .endLine(inputOracle.getEndLine())
                                        .expectedCommits(
                                                inputOracle.getExpectedCommits().stream().map(commit -> CommitUdt.builder()
                                                                .tracerName(TracerName.EXPECTED.getCode())
                                                                .commitHash(commit.getCommitHash())
                                                                .changeTags(commit.getChangeTags())
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

            log.info("save completed");
            return traceRepository.saveAll(traceEntityList);
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
            List<InputOracle> inputOracleEntityList = Arrays.stream(rootFileDir.listFiles())
                    .map(file -> {
                        try {
                            JsonNode json = objectMapper.readValue(file, JsonNode.class);

                            String repositoryName = json.get("repositoryName").asText();

                            List<InputCommit> commits = new ArrayList<>();
                            json.get("expectedResult")
                                    .fields()
                                    .forEachRemaining(commit -> {
                                        commits.add(InputCommit.builder().commitHash(commit.getKey()).changeTags(toChangeTags(commit.getValue().asText())).build());
                                    });
                            List<InputCommit> ideaCommits = Arrays.stream(json.get("intelliJ").asText().split(" "))
                                    .sorted(Comparator.reverseOrder())
                                    .map(hash -> InputCommit.builder().commitHash(hash).build()).toList();

                            HashMap<String, InputTrace> analysis = new HashMap<>();
                            analysis.put(TracerName.INTELLI_J.getCode(), InputTrace.builder().commits(ideaCommits).build());
                            InputOracle inputOracle = InputOracle.builder()
                                    .repositoryName(repositoryName)
                                    .repositoryUrl(repoMap.get(repositoryName))
                                    .startCommitHash(json.get("startCommitName").asText())
                                    .file(json.get("filePath").asText())
                                    .elementType("method")
                                    .element(json.get("functionName").asText())
                                    .startLine(json.get("functionStartLine").asInt())
                                    .endLine(json.get("functionEndLine").asInt())
                                    .expectedCommits(commits)
                                    //.analyzer(analysis)
                                    .build();
                            File outputFile = new File("./src/main/resources/stubs/java", Util.formatOracleFileId(fileNo.incrementAndGet()) + "-" + file.getName());
                            outputFile.createNewFile();

                            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, inputOracle);
                            objectMapper.readValue(file, InputOracle.class);
                            return inputOracle;

                        } catch (Exception e) {
                            log.error("File name {}", file.getName());
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void cleanDb() {
        traceRepository.deleteAll();
    }

    private String generateUid(InputOracle inputOracle) {
        String text = new StringBuilder()
                .append(inputOracle.getRepositoryName())
                .append(inputOracle.getRepositoryUrl())
                .append(inputOracle.getStartCommitHash())
                .append(inputOracle.getFile())
                .append(inputOracle.getElementType())
                .append(inputOracle.getElement())
                .append(inputOracle.getStartLine())
                .append(inputOracle.getEndLine())
                .toString();
        return DatatypeConverter.printHexBinary(DIGESTER.digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    private File[] findOracleFiles() {
        return new File(MethodTracker.class.getClassLoader().getResource("stubs/java").getFile())
                .listFiles();

    }

    private File findOracleFiles(Integer oracleFileId) {
        String formattedOracleFileId = Util.formatOracleFileId(oracleFileId);
        return Arrays.stream(findOracleFiles()).filter(file -> file.getName().startsWith(formattedOracleFileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Oracle file not found " + formattedOracleFileId));

    }

    private Set<ChangeTag> toChangeTags(String change) {
        Set<ChangeTag> changeTags = new TreeSet<>();
        if (change != null) {
            if (change.contains("Yintroduced")) {
                changeTags.add(ChangeTag.INTRODUCE);
            }
            if (change.contains("Ysignaturechange")) {
                changeTags.add(ChangeTag.SIGNATURE);
            }
            if (change.contains("Yrename")) {
                changeTags.add(ChangeTag.RENAME);
            }
            if (change.contains("Yreturntypechange")) {
                changeTags.add(ChangeTag.SIGNATURE);
                changeTags.add(ChangeTag.RETURN_TYPE);
            }
            if (change.contains("Yparameterchange")) {
                changeTags.add(ChangeTag.SIGNATURE);
                changeTags.add(ChangeTag.PARAMETER);
            }
            if (change.contains("Ymodifierchange")) {
                changeTags.add(ChangeTag.SIGNATURE);
                changeTags.add(ChangeTag.MODIFIER);
            }
            if (change.contains("Yexceptionschange")) {
                changeTags.add(ChangeTag.SIGNATURE);
                changeTags.add(ChangeTag.EXCEPTION);
            }

            if (change.contains("Ybodychange")) {
                changeTags.add(ChangeTag.BODY);
            }

            if (change.contains("Ymovefromfile")) {
                changeTags.add(ChangeTag.MOVE);
            }
            if (change.contains("Yfilerename")) {
                changeTags.add(ChangeTag.FILE_RENAME);
            }
        }
        return changeTags;
    }

}
