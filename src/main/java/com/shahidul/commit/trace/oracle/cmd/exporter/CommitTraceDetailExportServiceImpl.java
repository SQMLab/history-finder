package com.shahidul.commit.trace.oracle.cmd.exporter;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.cmd.helper.CommandLineHelperService;
import com.shahidul.commit.trace.oracle.cmd.writer.OutputFileWriter;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.aggregator.MetadataResolverService;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.core.service.executor.TraceExecutor;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
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
    MetadataResolverService metadataResolverService;
    TraceExecutor traceExecutor;


    @Override
    public void export(CommandLineInput commandLineInput) {
        String cacheDirectory = commandLineInput.getCacheDirectory();
        InputOracle inputOracle = commandLineHelperService.toInputOracle(commandLineInput);
        TraceEntity traceEntity = commandLineHelperService.loadOracle(inputOracle);
        TraceService targetTraceService = traceServiceList.stream()
                .filter(traceService -> traceService.getTracerName().equals(commandLineInput.getTracerName().getCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tracer not found"));
        traceExecutor.execute(traceEntity, targetTraceService);
        metadataResolverService.populateMetaData(traceEntity);
        CommitTraceOutput commitTraceOutput = commandLineHelperService.readOutput(traceEntity, commandLineInput.getTracerName());
        outputFileWriter.write(commandLineInput.getOutputFile(), commitTraceOutput);

    }
}
