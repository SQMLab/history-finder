package com.shahidul.git.log.oracle.core.service.analyzer;

import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceAnalysisEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    Set<String> expectedHashSet = traceEntity.getExpectedCommits().stream().map(CommitEntity::getCommitHash)
                            .collect(Collectors.toUnmodifiableSet());
                    Map<String, TraceAnalysisEntity> analysis = traceEntity.getAnalysis();
                    analysis
                            .values()
                            .stream()
                            //.filter()
                            .map(analysisEntity -> {
                                List<CommitEntity> correctCommits = analysisEntity.getCommits()
                                        .stream()
                                        .filter(commitEntity -> expectedHashSet.contains(commitEntity.getCommitHash()))
                                        .toList();
                                List<CommitEntity> incorrectCommits = analysisEntity.getCommits()
                                        .stream()
                                        .filter(commitEntity -> !expectedHashSet.contains(commitEntity.getCommitHash()))
                                        .toList();
                                Set<String> commitSet = analysisEntity.getCommits()
                                        .stream().map(CommitEntity::getCommitHash)
                                        .collect(Collectors.toSet());
                                List<CommitEntity> missingCommits = traceEntity.getExpectedCommits()
                                        .stream()
                                        .filter(commitEntity -> !commitSet.contains(commitEntity.getCommitHash()))
                                        .toList();
                                analysisEntity.setCorrectCommits(correctCommits);
                                analysisEntity.setIncorrectCommits(incorrectCommits);
                                int retrievedSize = correctCommits.size() + incorrectCommits.size();
                                if (commitSet.size() > 0) {
                                    analysisEntity.setPrecision( (double)correctCommits.size() / commitSet.size());
                                }
                                analysisEntity.setRecall( (double)correctCommits.size() / expectedHashSet.size());
                                analysisEntity.setMissingCommits(missingCommits);
                                return analysisEntity;
                            }).toList();
                    traceEntity.setPrecision(analysis.values()
                            .stream()
                            .map(TraceAnalysisEntity::getPrecision)
                            .mapToDouble(Double::doubleValue)
                            .average().getAsDouble());

                    traceEntity.setRecall(analysis.values()
                            .stream()
                            .map(TraceAnalysisEntity::getRecall)
                            .mapToDouble(Double::doubleValue)
                            .average().getAsDouble());

                    return traceEntity;
                }).toList();
        traceRepository.saveAll(modifiedTraceEntityList);
    }
}
