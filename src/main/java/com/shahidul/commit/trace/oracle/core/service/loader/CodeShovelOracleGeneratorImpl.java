package com.shahidul.commit.trace.oracle.core.service.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.cmd.writer.OutputFileWriter;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.config.CodeShovelOracleGeneratorConfiguration;
import com.shahidul.commit.trace.oracle.config.RepositoryNameUrlMappingConfiguration;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputCommit;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.InputTrace;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperServiceImpl;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.MethodTracker;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 14/7/24
 **/
@Service
@Slf4j
@AllArgsConstructor
public class CodeShovelOracleGeneratorImpl implements CodeShovelOracleGenerator {
    RepositoryNameUrlMappingConfiguration repositoryMappingConfiguration;
    CodeShovelOracleGeneratorConfiguration codeShovelOracleGeneratorConfiguration;
    AppProperty appProperty;
    ObjectMapper objectMapper;
    TraceDao traceDao;
    OracleHelperService oracleHelperService;
    OutputFileWriter outputFileWriter;

    @Override
    public void generate() {
        try {
            File rootFileDir = new File(codeShovelOracleGeneratorConfiguration.getStoredTraceDirectory());
            AtomicInteger fileNo = new AtomicInteger(codeShovelOracleGeneratorConfiguration.getStartOracleFileId());
            Arrays.stream(rootFileDir.listFiles())
                    .filter(traceDirectory -> codeShovelOracleGeneratorConfiguration.getRepositoryList().contains(traceDirectory.getName()))
                    .forEach(traceDirectory -> {
                        List<JsonNode> traceList = readAll(traceDirectory);
                        List<JsonNode> topTraceList = selectTop(traceList, codeShovelOracleGeneratorConfiguration.getMaximumOraclePerRepository());
                        for (JsonNode jsonOracle : topTraceList) {
                            InputOracle inputOracle = convertToOracleFile(jsonOracle);
                            String fileName = generateFileName(fileNo.getAndIncrement(), inputOracle);
                            outputFileWriter.write("./src/main/resources/oracle/" + fileName, inputOracle);
                        }
                    });

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<JsonNode> readAll(File traceDirectory) {
        return Arrays.stream(Objects.requireNonNull(traceDirectory.listFiles()))
                .map(traceFile -> {
                    try {
                        return objectMapper.readValue(traceFile, JsonNode.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    private List<JsonNode> selectTop(List<JsonNode> traceList, Integer maxLimit) {
        return traceList.stream().limit(maxLimit).toList();
    }

    private InputOracle convertToOracleFile(JsonNode json) {
        try {
            String repositoryName = json.get("repositoryName").asText();
            List<InputCommit> commits = new ArrayList<>();
            json.get("changeHistoryShort")
                    .fields()
                    .forEachRemaining(commit -> {
                        commits.add(InputCommit.builder().commitHash(commit.getKey()).changeTags(Util.toChangeTags(commit.getValue().asText())).build());
                    });

            return InputOracle.builder()
                    .repositoryName(repositoryName)
                    .repositoryUrl(repositoryMappingConfiguration.getRepositoryMapping().get(repositoryName))
                    .startCommitHash(json.get("startCommitName").asText())
                    .file(json.get("sourceFilePath").asText())
                    .elementType("method")
                    .element(json.get("functionName").asText())
                    .startLine(json.get("functionStartLine").asInt())
                    .endLine(json.get("functionEndLine").asInt())
                    .commits(commits)
                    .build();


        } catch (Exception e) {
            log.error("JSON to Input Oracle conversion error", e);
            throw new RuntimeException(e);
        }
    }

    private String generateFileName(Integer fileId, InputOracle inputOracle) {
        return String.join("-", Util.formatOracleFileId(fileId), inputOracle.getRepositoryName(), Util.extractClassNameFromFile(inputOracle.getFile()), inputOracle.getElement()) + ".json";
    }
}
