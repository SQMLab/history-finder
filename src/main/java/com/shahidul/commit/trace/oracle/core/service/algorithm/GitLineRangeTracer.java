package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.core.enums.TrackerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.GitTracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Shahidul Islam
 * @since 11/14/2023
 */
@Service
@AllArgsConstructor
@Slf4j
public class GitLineRangeTracer extends GitTracer {


    @Override
    public String getTracerName() {
        return TrackerName.GIT_LINE_RANGE.getCode();
    }

    @Override
    public String parseChangeType(String rawChangeType) {
        return rawChangeType;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {

        String gitLogCommand = String.format("git log %s --no-merges -L %s,%s:%s",
                traceEntity.getCommitHash(), traceEntity.getStartLine(), traceEntity.getEndLine(), traceEntity.getFilePath()) + " | grep 'commit\\s' | sed 's/commit//'";
        return super.trace(traceEntity, gitLogCommand);
    }
}
