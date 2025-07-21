package rnd.method.history.commit.trace.oracle.cmd.exporter;

import rnd.method.history.commit.trace.oracle.cmd.helper.CommandLineHelperService;
import rnd.method.history.commit.trace.oracle.cmd.model.CommandLineInput;
import rnd.method.history.commit.trace.oracle.cmd.writer.OutputFileWriter;
import rnd.method.history.commit.trace.oracle.core.model.InputOracle;
import rnd.method.history.commit.trace.oracle.core.mongo.dao.TraceDao;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.method.history.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import rnd.method.history.commit.trace.oracle.core.service.algorithm.TraceService;
import rnd.method.history.commit.trace.oracle.core.service.analyzer.TraceAnalyzer;
import rnd.method.history.commit.trace.oracle.core.service.executor.TraceExecutor;
import rnd.method.history.commit.trace.oracle.core.service.helper.OracleHelperService;
import rnd.method.history.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.CommitTraceOutput;

import java.util.List;

/**
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

        outputFileWriter.write(commandLineInput.getOutputFile(), execute(commandLineInput, false));

    }

    @Override
    public CommitTraceOutput execute(CommandLineInput commandLineInput, boolean useCache) {
        String cloneDirectory = commandLineInput.getCloneDirectory();
        InputOracle inputOracle = commandLineHelperService.toInputOracle(commandLineInput);
        TraceEntity traceEntity = commandLineHelperService.loadOracle(inputOracle, commandLineInput.getOracleFileId(), cloneDirectory, useCache);
        //Skip for oracles
        if (!traceEntity.getAnalysis().containsKey(commandLineInput.getTracerName().getCode()) || !useCache) {

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
