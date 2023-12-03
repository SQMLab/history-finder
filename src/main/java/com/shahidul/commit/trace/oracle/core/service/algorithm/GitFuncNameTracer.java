package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
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
//@Service
public class GitFuncNameTracer extends GitTracer {

    @Override
    public String getTracerName() {
        return TracerName.GIT_FUNC_NAME.getCode();
    }

    @Override
    public String parseChangeType(String rawChangeType) {
        return rawChangeType;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {

        String gitCommand = String.format("git log %s --no-merges -L /%s/:%s --no-patch",
                traceEntity.getStartCommitHash(), traceEntity.getElementName(), traceEntity.getFilePath()) + " | grep 'commit\\s' | sed 's/commit//'";

        return super.trace(traceEntity, gitCommand);

    }
}
