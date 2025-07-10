package com.shahidul.commit.trace.oracle.cmd.exporter;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.cmd.helper.CommandLineHelperService;
import com.shahidul.commit.trace.oracle.cmd.writer.OutputFileWriter;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import rnd.git.history.finder.dto.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Service
@AllArgsConstructor
public class CommitTraceDetailExportServiceImpl implements CommitTraceDetailExportService {
    List<TraceService> traceServiceList;
    TraceDao traceDao;
    OracleHelperService oracleHelperService;
    CommandLineHelperService commandLineHelperService;
    OutputFileWriter outputFileWriter;
    TraceAnalyzer traceAnalyzer;
    MetadataResolverService metadataResolverService;
    TraceExecutor traceExecutor;


    @Override
    public void export(CommandLineInput commandLineInput) {

        outputFileWriter.write(commandLineInput.getOutputFile(), execute(commandLineInput, true));

    }

    @Override
    public CommitTraceOutput execute(CommandLineInput commandLineInput, boolean forceExecute) {
        String cloneDirectory = commandLineInput.getCloneDirectory();
        InputOracle inputOracle = commandLineHelperService.toInputOracle(commandLineInput);
        TraceEntity traceEntity = commandLineHelperService.loadOracle(inputOracle, commandLineInput.getOracleFileId(), cloneDirectory);
        //Skip for oracles
        if (!traceEntity.getAnalysis().containsKey(commandLineInput.getTracerName().getCode()) || forceExecute) {

            TraceService targetTraceService = traceServiceList.stream()
                    .filter(traceService -> traceService.getTracerName().equals(commandLineInput.getTracerName().getCode()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Tracer not found"));
            traceExecutor.execute(traceEntity, targetTraceService);
            metadataResolverService.populateMetaData(traceEntity);
            traceAnalyzer.sortCommits(traceEntity);
        }
        CommitTraceOutput commitTraceOutput = commandLineHelperService.readOutput(traceEntity, commandLineInput.getTracerName());
        commitTraceOutput.setRepositoryFile(Util.concatPath(cloneDirectory, inputOracle.getRepositoryName()));
        return commitTraceOutput;
    }
}
