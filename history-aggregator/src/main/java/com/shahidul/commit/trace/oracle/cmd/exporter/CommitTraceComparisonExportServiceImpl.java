package com.shahidul.commit.trace.oracle.cmd.exporter;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.cmd.helper.CommandLineHelperService;
import com.shahidul.commit.trace.oracle.cmd.writer.OutputFileWriter;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.AggregatorTracer;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 5/1/2024
 */
@Service
@AllArgsConstructor
@Slf4j
public class CommitTraceComparisonExportServiceImpl implements CommitTraceComparisonExportService {

    List<TraceService> traceServiceList;
    TraceDao traceDao;
    OracleHelperService oracleHelperService;
    CommandLineHelperService commandLineHelperService;
    MetadataResolverService metadataResolverService;
    OutputFileWriter outputFileWriter;
    TraceExecutor traceExecutor;
    AggregatorTracer aggregatorTracer;
    TraceAnalyzer traceAnalyzer;

    @Override
    public void export(CommandLineInput commandLineInput) {
        String cloneDirectory = commandLineInput.getCloneDirectory();
        InputOracle inputOracle = commandLineHelperService.toInputOracle(commandLineInput);
        TraceEntity traceEntity = commandLineHelperService.loadOracle(inputOracle, null,cloneDirectory );
        TraceEntity finalTraceEntity = traceEntity;
        List<TraceEntity> traceEntityList = traceServiceList.stream()
                //.filter(traceService -> traceService.getTracerName().equals(TracerName.INTELLI_J.getCode()))
                .filter(traceService -> !traceService.getTracerName().equals(TracerName.INTELLI_J.getCode()) || finalTraceEntity.getOracleFileId() != null)
                .map(traceService -> traceExecutor.execute(finalTraceEntity, traceService))
                .toList();

        metadataResolverService.populateMetaData(finalTraceEntity);
        aggregatorTracer.trace(finalTraceEntity);
        traceAnalyzer.sortCommits(traceEntity);
        String csvText = buildCsv(traceEntity, commandLineInput.getTracerName());

        try {
            outputFileWriter.write(commandLineInput.getOutputFile(), csvText);
        } catch (Exception ex) {
            log.error("Failed to write into output file", ex);
        }
    }

    private String buildCsv(TraceEntity traceEntity, TracerName tracerName) {

        List<String> headerList = new ArrayList<>();
        Map<String, Set<String>> commitSetMap = new HashMap<>();


        AnalysisUdt aggregatedUdt = null;
        if (traceEntity.getAnalysis().containsKey(TracerName.AGGREGATED.getCode())) {
            headerList.add(TracerName.AGGREGATED.getCode());
            aggregatedUdt = traceEntity.getAnalysis().get(TracerName.AGGREGATED.getCode());
            commitSetMap.put(TracerName.AGGREGATED.getCode(), aggregatedUdt.getCommits().stream().map(CommitUdt::getCommitHash).collect(Collectors.toSet()));
        }

        if (traceEntity.getExpectedCommits() != null && !traceEntity.getExpectedCommits().isEmpty()) {
            headerList.add(TracerName.EXPECTED.getCode());
            Set<String> expectedCommitSet = traceEntity.getExpectedCommits().stream().map(CommitUdt::getCommitHash).collect(Collectors.toSet());
            commitSetMap.put(TracerName.EXPECTED.getCode(), expectedCommitSet);
            // addColumn(TracerName.EXPECTED.getCode(), traceEntity.getExpectedCommits(), headerBuilder, commitShawBuilder);
            String stronglyExpected = "stronglyExpected";
            headerList.add(stronglyExpected);
            Set<String> weaklyExpectedCommitSet = traceAnalyzer.getWeaklyExpectedCommitSet(traceEntity.getExpectedCommits(), tracerName)
                    .stream().map(CommitUdt::getCommitHash).collect(Collectors.toSet());
            Set<String> stronglyExpectedCommitSet = expectedCommitSet.stream()
                    .filter(commitHash -> !weaklyExpectedCommitSet.contains(commitHash))
                    .collect(Collectors.toSet());
            commitSetMap.put(stronglyExpected, stronglyExpectedCommitSet);
        }
        traceEntity.getAnalysis()
                .forEach((algoName, analysisUdt) -> {
                    if (!algoName.equalsIgnoreCase(TracerName.AGGREGATED.getCode())) {
                        headerList.add(algoName);
                        commitSetMap.put(algoName, analysisUdt.getCommits().stream().map(CommitUdt::getCommitHash).collect(Collectors.toSet()));
                    }
                });

        StringBuilder csvBuilder = new StringBuilder();
        for (int headerIndex = 0; headerIndex < headerList.size(); headerIndex++){
            String algoName = headerList.get(headerIndex);
            csvBuilder.append(algoName);
            if (headerIndex + 1 < headerList.size()){
                csvBuilder.append(",");
            }
        }
        csvBuilder.append("\n");

        aggregatedUdt.getCommits().forEach(commitUdt -> {
            String commitHash = commitUdt.getCommitHash();
            for (int headerIndex = 0; headerIndex < headerList.size(); headerIndex++){
                String algoName = headerList.get(headerIndex);
                if(commitSetMap.get(algoName).contains(commitHash)){
                    csvBuilder.append(commitHash);
                }
                if (headerIndex + 1 < headerList.size()){
                    csvBuilder.append(",");
                }
            }
            csvBuilder.append("\n");
        });
        return csvBuilder.toString();
    }

    private static void addColumn(String algoName, List<CommitUdt> commits, StringBuilder headerBuilder, StringBuilder commitShawBuilder) {
        if (!headerBuilder.isEmpty() || !commitShawBuilder.isEmpty()) {
            headerBuilder.append(",");
            commitShawBuilder.append(",");
        }
        headerBuilder.append(algoName);
        String commitShaw = commits
                .stream()
                .map(CommitUdt::getCommitHash)
                .collect(Collectors.joining("|"));
        commitShawBuilder.append(commitShaw);
    }
}
