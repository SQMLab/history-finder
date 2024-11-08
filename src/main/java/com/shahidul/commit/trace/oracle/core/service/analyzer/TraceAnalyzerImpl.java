package com.shahidul.commit.trace.oracle.core.service.analyzer;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Service
@AllArgsConstructor
@Slf4j
public class TraceAnalyzerImpl implements TraceAnalyzer {
    TraceDao traceDao;

    static final List<String> WEAK_RECALL_TRACER_LIST = Arrays.asList(TracerName.CODE_SHOVEL.getCode(),TracerName.CODE_TRACKER.getCode(), TracerName.GIT_LINE_RANGE.getCode(), TracerName.GIT_FUNC_NAME.getCode());
    static final List<ChangeTag> WEAKLY_EXPECTED_CHANGE_TAGS = Arrays.asList(ChangeTag.ANNOTATION, ChangeTag.FORMAT, ChangeTag.DOCUMENTATION);

    @Override
    @Transactional
    public TraceEntity analyze(TraceEntity traceEntity) {

        TreeSet<String> expectedHashSet = traceEntity.getExpectedCommits().stream().map(CommitUdt::getCommitHash)
                .collect(Collectors.toCollection(TreeSet::new));
        TreeSet<CommitUdt> weaklyExpectedHashSet = getWeaklyExpectedCommitSet(traceEntity.getExpectedCommits());


        Map<String, AnalysisUdt> analysis = traceEntity.getAnalysis();
        analysis.entrySet()
                .stream()
                //.filter()
                .map(entry -> {
                    AnalysisUdt analysisEntity = entry.getValue();
                    List<CommitUdt> correctCommits = analysisEntity.getCommits()
                            .stream()
                            .filter(commitEntity -> expectedHashSet.contains(commitEntity.getCommitHash()))
                            .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList();
                    Set<String> correctCommitSet = correctCommits.stream()
                            .map(CommitUdt::getCommitHash)
                            .collect(Collectors.toSet());
                    List<CommitUdt> incorrectCommits = analysisEntity.getCommits()
                            .stream()
                            .filter(commitEntity -> !expectedHashSet.contains(commitEntity.getCommitHash()))
                            .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList();
                    Set<String> commitSet = analysisEntity.getCommits()
                            .stream().map(CommitUdt::getCommitHash)
                            .collect(Collectors.toSet());
                    List<CommitUdt> missingCommits = traceEntity.getExpectedCommits()
                            .stream()
                            .filter(commitEntity -> !commitSet.contains(commitEntity.getCommitHash()))
                            .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList();
                    analysisEntity.setCommits(analysisEntity.getCommits()
                            .stream()
                            .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList());


                    analysisEntity.setCorrectCommits(correctCommits);
                    analysisEntity.setIncorrectCommits(incorrectCommits);
                    analysisEntity.setMissingCommits(missingCommits);
                    double precision = commitSet.isEmpty() ? 1 : (double) correctCommitSet.size() / commitSet.size();
                    analysisEntity.setPrecision(precision);
                    AtomicInteger preferredExpectedCommitSize = new AtomicInteger(expectedHashSet.size());
                    if (WEAK_RECALL_TRACER_LIST.contains(entry.getKey())) {
                        weaklyExpectedHashSet.forEach(commitUdt -> {
                            if (!expectedHashSet.contains(commitUdt.getCommitHash())){
                                preferredExpectedCommitSize.addAndGet(-1);
                            }
                        });
                    }
                    Double recall = null;
                    if (preferredExpectedCommitSize.get() > 0){
                        recall = (double) correctCommitSet.size() / preferredExpectedCommitSize.get();
                    }else {
                        recall = 1.0;
                    }
                    analysisEntity.setRecall(recall);
                    log.info("Tracer {}, expected {}, unexpected {}, preferred expected {}, correct {}, recall {}", entry.getKey(), expectedHashSet.size(), weaklyExpectedHashSet.size(), preferredExpectedCommitSize, correctCommits.size(), recall);
                    return analysisEntity;
                }).toList();
        traceEntity.setPrecision(analysis.values()
                .stream()
                .map(AnalysisUdt::getPrecision)
                .mapToDouble(Double::doubleValue)
                .average().getAsDouble());

        traceEntity.setRecall(analysis.values()
                .stream()
                .map(AnalysisUdt::getRecall)
                .mapToDouble(Double::doubleValue)
                .average().getAsDouble());

        return traceDao.save(traceEntity);

    }

    @Override
    public @NotNull TreeSet<CommitUdt> getWeaklyExpectedCommitSet(List<CommitUdt> expectedCommittList) {
        return expectedCommittList
                .stream()
                .filter(commitUdt -> !commitUdt.getChangeTags().isEmpty() &&
                        commitUdt.getChangeTags()
                                .stream()
                                .filter(changeTag -> !WEAKLY_EXPECTED_CHANGE_TAGS.contains(changeTag)).count() == 0)
                //.map(CommitUdt::getCommitHash)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
