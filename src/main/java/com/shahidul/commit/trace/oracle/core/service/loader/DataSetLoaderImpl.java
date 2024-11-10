package com.shahidul.commit.trace.oracle.core.service.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.InputTrace;
import com.shahidul.commit.trace.oracle.core.model.InputCommit;
import com.shahidul.commit.trace.oracle.core.model.StaticInputTrace;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperServiceImpl;
import com.shahidul.commit.trace.oracle.util.ChangeTagUtil;
import com.shahidul.commit.trace.oracle.util.Util;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
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
    AppProperty appProperty;
    ObjectMapper objectMapper;
    TraceDao traceDao;
    OracleHelperService oracleHelperService;
    Environment environment;

    @PostConstruct
    public void init() {
    }

    @Override
    public TraceEntity loadOracleFile(Integer oracleFileId) {
        TraceEntity traceEntity = traceDao.findByOracleId(oracleFileId);
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
            String oracleFileIdsText = environment.getProperty("run-config.oracle-file-ids", "1");
            Set<Integer> oracleFileIdList = new HashSet<>(Util.parseOracleFileIds(oracleFileIdsText));

            Map<String, TraceEntity> entityMap = traceDao.findAll()
                    .stream()
                    .collect(Collectors.toMap(TraceEntity::getOracleFileName, Function.identity()));

            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(findOracleFiles())
                    .filter(file -> {
                        Integer oracleFileId = Integer.valueOf(file.getName().substring(0, 3));
                        return oracleFileIdList.contains(oracleFileId);
                    })
                    .map(file -> {
                        try {
                            InputOracle inputOracle = objectMapper.readValue(file, InputOracle.class);
                            String oracleFileName = file.getName();
                            if (entityMap.containsKey(oracleFileName)) {
                                return entityMap.get(oracleFileName);
                            } else {
                                TraceEntity traceEntity = oracleHelperService.build(inputOracle);
                                traceEntity.setOracleFileId(Integer.valueOf(oracleFileName.substring(0, 3)));
                                traceEntity.setOracleFileName(oracleFileName);
                                return traceEntity;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            log.info("save completed");
            return traceDao.saveAll(traceEntityList);
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
                                        commits.add(InputCommit.builder().commitHash(commit.getKey()).changeTags(ChangeTagUtil.toChangeTagsFromCodeShovel(commit.getValue().asText())).build());
                                    });
                            List<InputCommit> ideaCommits = Arrays.stream(json.get("intelliJ").asText().split(" "))
                                    .sorted(Comparator.reverseOrder())
                                    .map(hash -> InputCommit.builder().commitHash(hash).build()).toList();

                            HashMap<String, InputTrace> analysis = new HashMap<>();
                            analysis.put(TracerName.INTELLI_J.getCode(), InputTrace.builder().commits(ideaCommits).build());
                            InputOracle inputOracle = InputOracle.builder()
                                    .repositoryName(repositoryName)
                                    .repositoryUrl(OracleHelperServiceImpl.repoMap.get(repositoryName))
                                    .startCommitHash(json.get("startCommitName").asText())
                                    .file(json.get("filePath").asText())
                                    .elementType("method")
                                    .element(json.get("functionName").asText())
                                    .startLine(json.get("functionStartLine").asInt())
                                    .endLine(json.get("functionEndLine").asInt())
                                    .commits(commits)
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
    public void processIntelliJInputDump() {
        try {
            File rootFileDir = new File(MethodTracker.class.getClassLoader().getResource("intellij").getFile());
            List<StaticInputTrace> inputOracleEntityList = Arrays.stream(rootFileDir.listFiles())
                    .map(file -> {
                        try {

                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                            List<InputCommit> commitList = bufferedReader.lines()
                                    .map(line -> {
                                        String[] parts = line.split("\t");
                                        return parts[0];
                                    })
                                    .map(commitHash -> InputCommit.builder().commitHash(commitHash).changeTags(new ArrayList<>()).build())
                                    .toList();
                            bufferedReader.close();



                            HashMap<String, InputTrace> analysis = new HashMap<>();
                            analysis.put(TracerName.INTELLI_J.getCode(), InputTrace.builder().commits(commitList).build());

                            StaticInputTrace inputTrace = StaticInputTrace.builder()
                                    .traceMap(analysis)
                                    .build();
                            File outputFile = new File("./src/main/resources/trace", file.getName());
                            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, inputTrace);
                            objectMapper.readValue(outputFile, StaticInputTrace.class);
                            return inputTrace;

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
        traceDao.deleteAll();
    }

    @Override
    public void updateExpectedCommit(List<TraceEntity> traceEntityList, TracerName fromTracer) {
        Map<String, File> fileMapping = Arrays.stream(findOracleFiles()).collect(Collectors.toMap(File::getName, file -> file));

        traceEntityList.forEach(traceEntity -> {
                    List<InputCommit> newExpectedCommits = traceEntity.getAnalysis().get(fromTracer.getCode()).getCommits().stream().map(commitUdt -> InputCommit.builder()
                            .commitHash(commitUdt.getCommitHash())
                            .changeTags(commitUdt.getChangeTags())
                            .build()).toList();
                    File oracleFile = fileMapping.get(traceEntity.getOracleFileName());
                    try {
                        InputOracle inputOracle = objectMapper.readValue(oracleFile, InputOracle.class);
                        inputOracle.setCommits(newExpectedCommits);
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("./src/main/resources/oracle/" + oracleFile.getName()), inputOracle );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    private File[] findOracleFiles() {
        return new File(DataSetLoader.class.getClassLoader().getResource(appProperty.getOracleFileDirectory()).getFile())
                .listFiles();

    }

    private File findOracleFiles(Integer oracleFileId) {
        String formattedOracleFileId = Util.formatOracleFileId(oracleFileId);
        return Arrays.stream(findOracleFiles()).filter(file -> file.getName().startsWith(formattedOracleFileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Oracle file not found " + formattedOracleFileId));

    }

    private void filterOutDeprecatedChangeTag(InputOracle inputOracle){
        Set<ChangeTag> changeTagSet = Set.of(ChangeTag.INTRODUCTION,
                ChangeTag.MOVE,
                ChangeTag.BODY,
                ChangeTag.DOCUMENTATION,
                ChangeTag.FILE_MOVE,
                ChangeTag.RENAME,
                ChangeTag.MODIFIER,
                ChangeTag.RETURN_TYPE,
                ChangeTag.EXCEPTION,
                ChangeTag.PARAMETER,
                ChangeTag.ANNOTATION,
                ChangeTag.FORMAT);

        inputOracle.getCommits()
                .forEach(commit-> {
                    Set<ChangeTag> updatedTagSet = commit.getChangeTags().stream()
                            .filter(changeTagSet::contains)
                            .collect(Collectors.toCollection(HashSet::new));

                   /* if (commit.getChangeTags().contains(ChangeTag.PACKAGE)||
                            commit.getChangeTags().contains(ChangeTag.FILE_RENAME)||
                            commit.getChangeTags().contains(ChangeTag.FILE_COPY)) {
                        updatedTagSet.add(ChangeTag.FILE_MOVE);
                    }
                    if (commit.getChangeTags().contains(ChangeTag.ACCESS_MODIFIER)) {
                        updatedTagSet.add(ChangeTag.MODIFIER);
                    }*/
                    List<ChangeTag> orderedTagList = new ArrayList<>(updatedTagSet);
                    commit.setChangeTags(orderedTagList);
                });
    }


    private void updateExpectedCommitTagSet(InputOracle inputOracle, TraceEntity traceEntity){
        List<TracerName> tracerNameList = List.of(TracerName.HISTORY_FINDER, TracerName.CODE_SHOVEL, TracerName.CODE_TRACKER);

        Map<TracerName, Map<String, List<ChangeTag>>> changeTagBundle = new HashMap<>();
        for (TracerName tracerName  : tracerNameList){
            Map<String, List<ChangeTag>> changeTagMap = traceEntity.getAnalysis().get(tracerName.getCode())
                    .getCommits()
                    .stream()
                    .collect(Collectors.toMap(CommitUdt::getCommitHash, CommitUdt::getChangeTags));
            changeTagBundle.put(tracerName, changeTagMap);

        }
        inputOracle.getCommits()
                .forEach(commit-> {
                    String commitHash = commit.getCommitHash();
                    Set<ChangeTag> updatedTagSet = new HashSet<>();
                    for (TracerName tracerName  : tracerNameList){
                        if (changeTagBundle.get(tracerName).containsKey(commitHash)){
                            List<ChangeTag> predictedChanges = changeTagBundle.get(tracerName).get(commitHash);
                            if (tracerName == TracerName.CODE_TRACKER && predictedChanges.size() == 1
                                    && predictedChanges.getFirst() == ChangeTag.DOCUMENTATION) {
                                updatedTagSet.clear();
                                updatedTagSet.addAll(predictedChanges);
                                break;
                            }
                            updatedTagSet.addAll(predictedChanges);
                        }
                    }

                    List<ChangeTag> orderedTagList = new ArrayList<>(updatedTagSet);
                    orderedTagList.sort(ChangeTag.NATURAL_ORDER);
                    commit.setChangeTags(orderedTagList);
                });

    }

    @Override
    public List<TraceEntity> updateCommitChangeTag() {
        log.info("loading data set ..");
        //ClassPathResource classPathResource = new ClassPathResource("classpath:oracle/method/training", MethodTracker.class.getClassLoader());
        try {
            String oracleFileIdsText = environment.getProperty("run-config.oracle-file-ids", "1");
            Set<Integer> oracleFileIdList = new HashSet<>(Util.parseOracleFileIds(oracleFileIdsText));

            Map<String, TraceEntity> entityMap = traceDao.findAll()
                    .stream()
                    .collect(Collectors.toMap(TraceEntity::getOracleFileName, Function.identity()));

            //rootFileDir = classPathResource.getFile();
            List<TraceEntity> traceEntityList = Arrays.stream(findOracleFiles())
                    .filter(file -> {
                        Integer oracleFileId = Integer.valueOf(file.getName().substring(0, 3));
                        return oracleFileIdList.contains(oracleFileId);
                    })
                    .map(file -> {
                        try {
                            InputOracle inputOracle = objectMapper.readValue(file, InputOracle.class);
/*
                            filterOutDeprecatedChangeTag(inputOracle);
                            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, inputOracle);*/
                            String oracleFileName = file.getName();

                            if (entityMap.containsKey(oracleFileName)) {
                                TraceEntity traceEntity = entityMap.get(oracleFileName);
                                updateExpectedCommitTagSet(inputOracle, traceEntity);
                                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("./src/main/resources/oracle", oracleFileName), inputOracle);
                                return traceEntity;

                            } else {
                                TraceEntity traceEntity = oracleHelperService.build(inputOracle);
                                traceEntity.setOracleFileId(Integer.valueOf(oracleFileName.substring(0, 3)));
                                traceEntity.setOracleFileName(oracleFileName);
                                return traceEntity;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(file.getName(), e);
                        }
                    }).collect(Collectors.toList());

            log.info("save completed");
            //return traceDao.saveAll(traceEntityList);
            return traceEntityList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
