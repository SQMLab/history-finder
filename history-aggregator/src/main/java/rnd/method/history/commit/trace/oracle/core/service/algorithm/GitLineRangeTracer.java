package rnd.method.history.commit.trace.oracle.core.service.algorithm;

import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.ChangeTag;

/**
 * @since 11/14/2023
 */
@Service("GIT_LINE_RANGE")
@AllArgsConstructor
@Slf4j
public class GitLineRangeTracer extends GitTracer {

    @Override
    public String getTracerName() {
        return TracerName.GIT_LINE_RANGE.getCode();
    }

    @Override
    public ChangeTag parseChangeType(String rawChangeType) {
        return null;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {

        String gitLogCommand = String.format("git log %s --no-merges --histogram %s -L %s,%s:%s",
                traceEntity.getStartCommitHash(), LOG_FORMAT, traceEntity.getStartLine(), traceEntity.getEndLine(), traceEntity.getFile());
        return super.trace(traceEntity, gitLogCommand);
    }
}
