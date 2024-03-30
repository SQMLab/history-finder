package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Service
@AllArgsConstructor
@Slf4j
public class AggregatorTracer implements TraceService {
    TraceRepository traceRepository;
    AppProperty appProperty;


    @Override
    public String getTracerName() {
        return TracerName.AGGREGATED.getCode();
    }

    @Override
    public ChangeTag parseChangeType(String rawChangeType) {
        return null;
    }

    @Override
    @Transactional
    public TraceEntity trace(TraceEntity traceEntity) {
        List<CommitUdt> aggregatedList = traceEntity.getAnalysis()
                .entrySet()
                .stream()
                .filter(analysisEntry -> !TracerName.AGGREGATED.getCode().equals(analysisEntry.getKey()))
                .map(analysisEntry -> analysisEntry.getValue())
                .map(AnalysisUdt::getCommits)
                .flatMap(List::stream)
                .collect(Collectors.toMap(CommitUdt::getCommitHash, Function.identity(), (o1, o2) -> {
                    TracerName trackerX = TracerName.fromCode(o1.getTracerName());
                    TracerName trackerY = TracerName.fromCode(o2.getTracerName());
                    if (TracerName.AGGREGATION_PRIORITY.indexOf(trackerX) < TracerName.AGGREGATION_PRIORITY.indexOf(trackerY)) {
                        return o1;
                    } else {
                        return o2;
                    }
                }))
                .values()
                .stream()
                .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsFirst(Comparator.reverseOrder())))
                .map(commitUdt -> {
                    try {
                        CommitUdt clonedCommit = (CommitUdt) commitUdt.clone();
                        clonedCommit.setTracerName(TracerName.AGGREGATED.getCode());
                        return clonedCommit;
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        long overallRunningTime = traceEntity.getAnalysis()
                .entrySet()
                .stream()
                .filter(analysisEntry -> !TracerName.AGGREGATED.getCode().equals(analysisEntry.getKey()))
                .map(analysisEntry -> analysisEntry.getValue())
                .map(AnalysisUdt::getRuntime)
                .mapToLong(runningTime -> {
                    if (runningTime == null) {
                        return 0L;
                    } else {
                        return runningTime.longValue();
                    }
                })
                .sum();
        AnalysisUdt analysisUdt = AnalysisUdt.builder()
                .runtime(overallRunningTime)
                .commits(aggregatedList)
                .build();
        traceEntity.getAnalysis().put(TracerName.AGGREGATED.getCode(), analysisUdt);
        return traceRepository.save(traceEntity);
    }
}
