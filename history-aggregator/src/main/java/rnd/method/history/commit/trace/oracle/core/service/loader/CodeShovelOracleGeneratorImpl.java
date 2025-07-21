package rnd.method.history.commit.trace.oracle.core.service.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import rnd.method.history.commit.trace.oracle.cmd.writer.OutputFileWriter;
import rnd.method.history.commit.trace.oracle.config.AppProperty;
import rnd.method.history.commit.trace.oracle.config.CodeShovelOracleGeneratorConfiguration;
import rnd.method.history.commit.trace.oracle.config.RepositoryNameUrlMappingConfiguration;
import rnd.git.history.finder.dto.ChangeTag;
import rnd.git.history.finder.dto.InputCommit;
import rnd.git.history.finder.enums.LanguageType;
import rnd.method.history.commit.trace.oracle.core.model.InputOracle;
import rnd.method.history.commit.trace.oracle.core.mongo.dao.TraceDao;
import rnd.method.history.commit.trace.oracle.core.service.helper.OracleHelperService;
import rnd.git.history.finder.util.ChangeTagUtil;
import rnd.method.history.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @since 14/7/24
 **/
@Service
@Slf4j
@AllArgsConstructor
public class CodeShovelOracleGeneratorImpl implements CodeShovelOracleGenerator {
    private static final Set<ChangeTag> DIFFICULT_CHANGE_TAG_SET = Set.of(
            ChangeTag.FILE_MOVE,
            ChangeTag.MOVE,
            ChangeTag.RENAME);
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
            Map<String, String> startCommitHashMapping = getStartCommitHashMapping();
            File rootFileDir = new File(codeShovelOracleGeneratorConfiguration.getStoredTraceDirectory());
            AtomicInteger fileNo = new AtomicInteger(codeShovelOracleGeneratorConfiguration.getStartOracleFileId());
            Arrays.stream(rootFileDir.listFiles())
                    .filter(traceDirectory -> codeShovelOracleGeneratorConfiguration.getRepositoryList().contains(traceDirectory.getName()))
                    .forEach(traceDirectory -> {
                        List<JsonNode> traceList = readAll(traceDirectory, startCommitHashMapping);
                        List<JsonNode> topTraceList = selectTop(traceList, codeShovelOracleGeneratorConfiguration.getMaximumOraclePerRepository(), traceDirectory.getName());
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

    private List<JsonNode> readAll(File traceDirectory, Map<String, String> startCommitHashMapping) {
        return Arrays.stream(Objects.requireNonNull(traceDirectory.listFiles()))
                .map(traceFile -> {
                    try {
                        JsonNode jsonNode = objectMapper.readValue(traceFile, JsonNode.class);
                        if ("HEAD".equalsIgnoreCase(jsonNode.get("startCommitName").asText())) {
                            ((ObjectNode) jsonNode).put("startCommitName", startCommitHashMapping.get(jsonNode.get("repositoryName").asText()));
                        }
                        return jsonNode;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<JsonNode> selectTop(List<JsonNode> traceList, Integer maxLimit, String repositoryName) {


        AtomicInteger totalIntendedOpsCount = new AtomicInteger();
        AtomicInteger totalLineNumberCount = new AtomicInteger(0);
        traceList.forEach(json -> {
            Integer intendedOpsCount = countIntendedOps(json);
            ((ObjectNode) json).put("intendedOpsCount", intendedOpsCount);
            totalIntendedOpsCount.addAndGet(intendedOpsCount);

            int lineNumberCount = countMethodLine(json);
            ((ObjectNode) json).put("methodLineCount", lineNumberCount);
            totalLineNumberCount.addAndGet(lineNumberCount);
        });

        double intendedOpsFactor = (totalIntendedOpsCount.get() == 0 ? 0 : 1.0 / totalIntendedOpsCount.get());
        double lineNumberFactor = (totalLineNumberCount.get() == 0 ? 0 : 1.0 / totalLineNumberCount.get());
        traceList.sort((x, y) -> {
            double priorityX = x.get("intendedOpsCount").asInt() * intendedOpsFactor * 0.60 - x.get("methodLineCount").asInt() * lineNumberFactor * 0.40;
            double priorityY = y.get("intendedOpsCount").asInt() * intendedOpsFactor * 0.60 - y.get("methodLineCount").asInt() * lineNumberFactor * 0.40;
            return Double.compare(priorityX, priorityY) * -1;

        });
        Set<String> filsSet = new HashSet<>();
        List<JsonNode> topList = new ArrayList<>();
        for (int i = 0; i < traceList.size() && topList.size() < maxLimit; i++){
            JsonNode jsonNode = traceList.get(i);
            String file = jsonNode.get("sourceFilePath").asText();
            if (!filsSet.contains(file)){
                filsSet.add(file);
                topList.add(jsonNode);
            }
        }
        return topList;
    }

    private InputOracle convertToOracleFile(JsonNode json) {
        try {
            String repositoryName = json.get("repositoryName").asText();
            List<InputCommit> commits = new ArrayList<>();
            json.get("changeHistoryShort")
                    .fields()
                    .forEachRemaining(commit -> {
                        commits.add(InputCommit.builder().commitHash(commit.getKey()).changeTags(ChangeTagUtil.toChangeTagsFromCodeShovel(commit.getValue().asText())).build());
                    });

            return InputOracle.builder()
                    .repositoryName(repositoryName)
                    .repositoryUrl(repositoryMappingConfiguration.getRepositoryMapping().get(repositoryName))
                    .language(LanguageType.JAVA.getCode())
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


    private Integer countIntendedOps(JsonNode json) {
        List<InputCommit> commits = new ArrayList<>();
        json.get("changeHistoryShort")
                .fields()
                .forEachRemaining(commit -> {
                    commits.add(InputCommit.builder().commitHash(commit.getKey()).changeTags(ChangeTagUtil.toChangeTagsFromCodeShovel(commit.getValue().asText())).build());
                });
        AtomicInteger difficultChangeTagCount = new AtomicInteger(0);
        commits.forEach(commit -> commit.getChangeTags().forEach(changeTag -> {
            if (DIFFICULT_CHANGE_TAG_SET.contains(changeTag)) {
                difficultChangeTagCount.incrementAndGet();
            }
        }));
        return difficultChangeTagCount.get();
    }

    private int countMethodLine(JsonNode jsonNode) {
        return jsonNode.get("functionEndLine").asInt() - jsonNode.get("functionStartLine").asInt() + 1;
    }

    private String generateFileName(Integer fileId, InputOracle inputOracle) {
        return String.join("-", Util.formatOracleFileId(fileId), inputOracle.getRepositoryName(), Util.extractClassNameFromFile(inputOracle.getFile()), inputOracle.getElement()) + ".json";
    }


    private Map<String, String> getStartCommitHashMapping() {
        Map<String, String> startCommitMapping = new HashMap<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(codeShovelOracleGeneratorConfiguration.getHeadPointerMappingFile());
            List<String> lineList = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);
            fileInputStream.close();
            lineList.forEach(line -> {
                String[] parts = line.split("\t");
                startCommitMapping.put(parts[0], parts[2]);
            });
        } catch (IOException e) {
            log.error("Start commit hash mapping file parse error", e);
            throw new RuntimeException(e);
        }
        return startCommitMapping;
    }
}
