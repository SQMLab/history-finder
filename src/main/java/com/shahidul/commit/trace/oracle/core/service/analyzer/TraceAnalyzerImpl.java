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

    static final Map<String, List<ChangeTag>> WEAKLY_EXPECTED_CHANGE_TAG_MAPPING = Map.of(
            TracerName.CODE_SHOVEL.getCode(),Arrays.asList(ChangeTag.ANNOTATION, ChangeTag.FORMAT, ChangeTag.DOCUMENTATION),
            TracerName.INTELLI_J.getCode(),Arrays.asList( ChangeTag.ANNOTATION, ChangeTag.DOCUMENTATION),
            TracerName.GIT_LINE_RANGE.getCode(),Arrays.asList(ChangeTag.ANNOTATION, ChangeTag.DOCUMENTATION),
            TracerName.GIT_FUNC_NAME.getCode(),Arrays.asList(ChangeTag.ANNOTATION, ChangeTag.DOCUMENTATION),
            TracerName.CODE_TRACKER.getCode(),Arrays.asList( ChangeTag.FORMAT, ChangeTag.DOCUMENTATION)
            );
    @Override
    @Transactional
    public TraceEntity analyze(TraceEntity traceEntity) {

        Set<String> expectedHashSet = traceEntity.getExpectedCommits().stream().map(CommitUdt::getCommitHash)
                .collect(Collectors.toCollection(HashSet::new));


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

                    analysisEntity.setCommits(analysisEntity.getCommits()
                            .stream()
                            .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList());


                    analysisEntity.setCorrectCommits(correctCommits);
                    analysisEntity.setIncorrectCommits(incorrectCommits);
                    double precision = commitSet.isEmpty() ? 1 : (double) correctCommitSet.size() / commitSet.size();
                    analysisEntity.setPrecision(precision);
                    List<CommitUdt> missingCommits = new ArrayList<>();
                    Set<CommitUdt> weaklyExpectedHashSet = getWeaklyExpectedCommitSet(traceEntity.getExpectedCommits(), TracerName.fromCode(entry.getKey()));

                    Set<String> weaklyExpectedCommitSet = weaklyExpectedHashSet.stream().map(CommitUdt::getCommitHash).collect(Collectors.toSet());

                    traceEntity.getExpectedCommits()
                            .forEach(commitUdt -> {
                                if(!commitSet.contains(commitUdt.getCommitHash())){
                                    if (!weaklyExpectedCommitSet.contains(commitUdt.getCommitHash())){
                                        missingCommits.add(commitUdt);
                                    }
                                }
                            });
                    missingCommits.sort(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                    int preferredExpectedCommitSize = correctCommits.size() + missingCommits.size();

                    analysisEntity.setMissingCommits(missingCommits);

                    if (expectedHashSet.size() != preferredExpectedCommitSize){
                        log.info("Expected commit Size {} and preferred commit size {}", expectedHashSet.size(), preferredExpectedCommitSize);
                    }
                    Double recall = null;
                    if (preferredExpectedCommitSize > 0){
                        recall = (double) correctCommitSet.size() / preferredExpectedCommitSize;
                    }else {
                        recall = 1.0;
                    }
                    analysisEntity.setRecall(recall);
                    Double f1Score = 2 * precision * recall / (precision + recall);
                    analysisEntity.setF1Score(f1Score);
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
    public @NotNull Set<CommitUdt> getWeaklyExpectedCommitSet(List<CommitUdt> expectedCommittList, TracerName tracerName) {
        List<ChangeTag> weaklyExpectedTags = WEAKLY_EXPECTED_CHANGE_TAG_MAPPING.getOrDefault(tracerName.getCode(), Collections.emptyList());
        return expectedCommittList
                .stream()
                .filter(commitUdt -> !commitUdt.getChangeTags().isEmpty() &&
                        commitUdt.getChangeTags()
                                .stream()
                                .filter(changeTag -> !weaklyExpectedTags.contains(changeTag)).count() == 0)
                //.map(CommitUdt::getCommitHash)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
