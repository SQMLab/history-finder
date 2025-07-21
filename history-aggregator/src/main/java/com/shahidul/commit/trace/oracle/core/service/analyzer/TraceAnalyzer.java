package com.shahidul.commit.trace.oracle.core.service.analyzer;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @since 11/13/2023
 */
public interface TraceAnalyzer {
    TraceEntity analyze(TraceEntity traceEntity);
    TraceEntity sortCommits(TraceEntity traceEntity);

    @NotNull
    Set<CommitUdt> getWeaklyExpectedCommitSet(List<CommitUdt> expectedCommittList, TracerName tracerName);
}
