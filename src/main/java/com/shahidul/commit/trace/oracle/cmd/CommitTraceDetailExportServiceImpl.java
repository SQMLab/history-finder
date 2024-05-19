package com.shahidul.commit.trace.oracle.cmd;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
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
    @Override
    public void export(CommandLineInput commandLineInput) {
        String cacheDirectory = commandLineInput.getCacheDirectory();
        InputOracle inputOracle = commandLineHelperService.toInputOracle(commandLineInput);
        TraceEntity traceEntity = commandLineHelperService.loadOracle(inputOracle);
        TraceEntity finalTraceEntity = traceEntity;
        List<TraceEntity> traceEntityList = traceServiceList.stream()
                //.filter(traceService -> traceService.getTracerName().equals(TracerName.INTELLI_J.getCode()))
                .filter(traceService -> !traceService.getTracerName().equals(TracerName.INTELLI_J.getCode()) || finalTraceEntity.getOracleFileId() != null)
                .map(traceService -> traceService.trace(finalTraceEntity))
                .toList();
    }
}
