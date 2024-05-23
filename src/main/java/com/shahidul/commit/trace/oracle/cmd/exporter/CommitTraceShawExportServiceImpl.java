package com.shahidul.commit.trace.oracle.cmd.exporter;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.cmd.helper.CommandLineHelperService;
import com.shahidul.commit.trace.oracle.cmd.writer.OutputFileWriter;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 5/1/2024
 */
@Service
@AllArgsConstructor
@Slf4j
public class CommitTraceShawExportServiceImpl implements CommitTraceShawExportService {

    List<TraceService> traceServiceList;
    TraceDao traceDao;
    OracleHelperService oracleHelperService;
    CommandLineHelperService commandLineHelperService;

    OutputFileWriter outputFileWriter;
    TraceExecutor traceExecutor;

    @Override
    public void export(CommandLineInput commandLineInput) {
        String cacheDirectory = commandLineInput.getCacheDirectory();
        InputOracle inputOracle = commandLineHelperService.toInputOracle(commandLineInput);
        TraceEntity traceEntity = commandLineHelperService.loadOracle(inputOracle, null);
        TraceEntity finalTraceEntity = traceEntity;
        List<TraceEntity> traceEntityList = traceServiceList.stream()
                //.filter(traceService -> traceService.getTracerName().equals(TracerName.INTELLI_J.getCode()))
                .filter(traceService -> !traceService.getTracerName().equals(TracerName.INTELLI_J.getCode()) || finalTraceEntity.getOracleFileId() != null)
                .map(traceService -> traceExecutor.execute(finalTraceEntity, traceService))
                .toList();

        String csvText = buildCsv(traceEntity);

        try {
            outputFileWriter.write(commandLineInput.getOutputFile(), csvText);
        }catch (Exception ex){
            log.error("Failed to write into output file", ex);
        }
    }

    private String buildCsv(TraceEntity traceEntity) {
        StringBuilder headerBuilder = new StringBuilder();
        StringBuilder commitShawBuilder = new StringBuilder();
        traceEntity.getAnalysis()
                .forEach((algoName, analysis) -> {
                    if (headerBuilder.length() > 0 || commitShawBuilder.length() > 0){
                        headerBuilder.append(",");
                        commitShawBuilder.append(",");
                    }
                    headerBuilder.append(algoName);
                    String commitShaw = analysis.getCommits()
                            .stream()
                            .map(CommitUdt::getCommitHash)
                            .collect(Collectors.joining("|"));
                    commitShawBuilder.append(commitShaw);
                });
        return headerBuilder + "\n" + commitShawBuilder;
    }
}
