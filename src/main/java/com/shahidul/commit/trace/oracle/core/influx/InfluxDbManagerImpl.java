package com.shahidul.commit.trace.oracle.core.influx;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.influx.repository.AnalysisSeriesRepository;
import com.shahidul.commit.trace.oracle.core.influx.repository.CommitSeriesRepository;
import com.shahidul.commit.trace.oracle.core.influx.series.AnalysisSeries;
import com.shahidul.commit.trace.oracle.core.influx.series.CommitSeries;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 12/3/2023
 */
@Repository
@AllArgsConstructor
public class InfluxDbManagerImpl implements InfluxDbManager {
    TraceRepository traceRepository;
    AnalysisSeriesRepository analysisSeriesRepository;
    CommitSeriesRepository commitSeriesRepository;

    @Override
    public void load() {
        analysisSeriesRepository.deleteAll();
        commitSeriesRepository.deleteAll();

        traceRepository.findAll()
                .stream()
                .map(traceEntity -> {
                    List<CommitUdt> commitUdtList = traceEntity.getAnalysis()
                            .entrySet()
                            .stream()
                            .map(entry -> entry.getValue()
                                    .getCommits()
                                    .stream()
                                    .toList())
                            .flatMap(Collection::stream)
                            .toList();
                    List<CommitUdt> allCommitList = new ArrayList<>(commitUdtList);
                    allCommitList.addAll(traceEntity.getExpectedCommits());
                    commitSeriesRepository.saveAll(allCommitList.stream().map(commitUdt -> toCommitSeries(traceEntity, commitUdt)).toList());

                    Stream<AnalysisSeries> analysisSeriesStream = traceEntity.getAnalysis()
                            .entrySet()
                            .stream()
                            .map(analysisEntry -> toAnalysisSeries(traceEntity, analysisEntry.getKey(), analysisEntry.getValue() ));

                    AnalysisUdt dummyExpectedAnalysisEntity = AnalysisUdt.builder()
                            .commits(traceEntity.getExpectedCommits())
                            .correctCommits(traceEntity.getExpectedCommits())
                            .incorrectCommits(new ArrayList<>())
                            .missingCommits(new ArrayList<>())
                            .precision(1.0)
                            .recall(1.0)
                            .runtime(1L)
                            .build();
                    List<AnalysisSeries> analysisSeriesList = Stream.concat(analysisSeriesStream,
                            Stream.of(toAnalysisSeries(traceEntity, TracerName.EXPECTED.getCode(), dummyExpectedAnalysisEntity)))
                            .toList();
                    analysisSeriesRepository.saveAll(analysisSeriesList);
                    return traceEntity;
                }).toList();

    }

    private CommitSeries toCommitSeries(TraceEntity traceEntity, CommitUdt commitUdt) {
        return CommitSeries.builder()
                .oracleFileId(traceEntity.getOracleFileId())
                .oracleFileName(traceEntity.getOracleFileName())
                .tracerName(commitUdt.getTracerName())
                .commitHash(commitUdt.getCommitHash().substring(0, 4))
                .committedAt(commitUdt.getCommittedAt().toInstant())
                .build();
    }

    private AnalysisSeries toAnalysisSeries(TraceEntity traceEntity, String tracerName, AnalysisUdt analysisUdt) {
        return AnalysisSeries.builder()
                .tracerName(tracerName)
                .oracleFileId(traceEntity.getOracleFileId())
                .oracleFileName(traceEntity.getOracleFileName())
                .precision(analysisUdt.getPrecision())
                .recall(analysisUdt.getRecall())
                .runtime(analysisUdt.getRuntime())
                .commitCount(analysisUdt.getCommits().size())
                .correctCommitCount(analysisUdt.getCorrectCommits().size())
                .incorrectCommitCount(analysisUdt.getIncorrectCommits().size())
                .missingCommitCount(analysisUdt.getMissingCommits().size())
                .createdAt(LocalDateTime.now().minusYears(20).plusMonths(traceEntity.getOracleFileId()).toInstant(ZoneOffset.UTC))
                .build();
    }
}
