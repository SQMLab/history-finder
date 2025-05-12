package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputTrace;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.storage.StaticTraceDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/23/2024
 */
@AllArgsConstructor
@Service
public class IntelliJTraceService implements TraceService {
    StaticTraceDao staticTraceDao;

    @Override
    public String getTracerName() {
        return TracerName.INTELLI_J.getCode();
    }

    @Override
    public ChangeTag parseChangeType(String rawChangeType) {
        return null;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {

        InputTrace trace = staticTraceDao.findTrace(traceEntity.getOracleFileName(), TracerName.INTELLI_J);

        List<CommitUdt> commitList = trace.getCommits()
                .stream()
                .map(commit -> CommitUdt.builder()
                        .tracerName(getTracerName())
                        .commitHash(commit.getCommitHash())
                        .changeTags(commit.getChangeTags())
                        .build())
                .toList();
        traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder().commits(commitList).build());
        return traceEntity;
    }
}
