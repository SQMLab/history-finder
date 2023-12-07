package com.shahidul.commit.trace.oracle.core.service.analyzer;

import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Service
@AllArgsConstructor
public class TraceAnalyzerImpl implements TraceAnalyzer {
    TraceRepository traceRepository;

    @Override
    @Transactional
    public void analyze() {
        List<TraceEntity> modifiedTraceEntityList = traceRepository.findAll()
                .stream()
                .map(traceEntity -> {
                    Set<String> expectedHashSet = traceEntity.getExpectedCommits().stream().map(CommitUdt::getCommitHash)
                            .collect(Collectors.toUnmodifiableSet());
                    Map<String, AnalysisUdt> analysis = traceEntity.getAnalysis();
                    analysis
                            .values()
                            .stream()
                            //.filter()
                            .map(analysisEntity -> {
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
                                analysisEntity.setPrecision((double) correctCommitSet.size() / commitSet.size());
                                analysisEntity.setRecall((double) correctCommitSet.size() / expectedHashSet.size());
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

                    return traceEntity;
                }).toList();
        traceRepository.saveAll(modifiedTraceEntityList);
    }
}
