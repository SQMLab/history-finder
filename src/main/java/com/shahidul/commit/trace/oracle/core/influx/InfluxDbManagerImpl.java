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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 12/3/2023
 */
@Repository
@AllArgsConstructor
@Slf4j
public class InfluxDbManagerImpl implements InfluxDbManager {
    TraceRepository traceRepository;
    AnalysisSeriesRepository analysisSeriesRepository;
    CommitSeriesRepository commitSeriesRepository;

    @Override
    public void load() {
        analysisSeriesRepository.deleteAll();
        commitSeriesRepository.deleteAll();

        List<TraceEntity> traceEntityList = traceRepository.findAll();
        LocalDateTime analysisSeriesDateTime = LocalDate.ofYearDay(2023, 1).atStartOfDay();


        traceEntityList
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
                    Map<String, Instant> timeMap = determineTimeAlignment(allCommitList);
                    commitSeriesRepository.saveAll(allCommitList.stream().map(commitUdt -> toCommitSeries(traceEntity, commitUdt, timeMap)).toList());


                    Stream<AnalysisSeries> analysisSeriesStream = traceEntity.getAnalysis()
                            .entrySet()
                            .stream()
                            .map(analysisEntry -> toAnalysisSeries(traceEntity, analysisEntry.getKey(), analysisEntry.getValue(), analysisSeriesDateTime.plusDays(traceEntity.getOracleFileId() - 1).toInstant(ZoneOffset.UTC)));

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
                                    Stream.of(toAnalysisSeries(traceEntity, TracerName.EXPECTED.getCode(), dummyExpectedAnalysisEntity, analysisSeriesDateTime.plusDays(traceEntity.getOracleFileId() - 1).toInstant(ZoneOffset.UTC))))
                            .toList();
                    analysisSeriesRepository.saveAll(analysisSeriesList);
                    return traceEntity;
                }).toList();

    }

    private CommitSeries toCommitSeries(TraceEntity traceEntity, CommitUdt commitUdt, Map<String, Instant> timeMap) {
        return CommitSeries.builder()
                .oracleFileId(traceEntity.getOracleFileId())
                .oracleFileName(traceEntity.getOracleFileName())
                .tracerName(commitUdt.getTracerName())
                .commitHash(commitUdt.getCommitHash().substring(0, 4))
                .committedAt(commitUdt.getCommittedAt() == null ? Instant.now() : commitUdt.getCommittedAt().toInstant())
                .diff(commitUdt.getDiff())
                .diffDetail(commitUdt.getDiffDetail())
                .createdAt(timeMap.get(commitUdt.getCommitHash()))
                .build();
    }

    private AnalysisSeries toAnalysisSeries(TraceEntity traceEntity, String tracerName, AnalysisUdt analysisUdt, Instant createdAt) {
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
                //.createdAt(LocalDateTime.now().minusYears(20).plusMonths(traceEntity.getOracleFileId()).toInstant(ZoneOffset.UTC))
                .createdAt(createdAt)
                .build();
    }

    Map<String, Instant> determineTimeAlignment(List<CommitUdt> allCommitList) {
        LocalDateTime movingDate = LocalDate.ofYearDay(2023, 1).atStartOfDay();

        AtomicLong dayIndex = new AtomicLong(0);
        Map<String, Instant> timeMap = new HashMap<>();
        allCommitList.stream()
                .filter(commitUdt -> commitUdt.getCommittedAt() != null)
                .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(commitUdt -> {
                    if (!timeMap.containsKey(commitUdt.getCommitHash())) {
                        timeMap.put(commitUdt.getCommitHash(), movingDate.plusDays(dayIndex.getAndIncrement()).toInstant(ZoneOffset.UTC));
                    }
                    return commitUdt;
                }).toList();

        for (int i = 0; i < 10; i++)
            dayIndex.getAndIncrement();//Keep nulls in days apart
        allCommitList.stream()
                .filter(commitUdt -> commitUdt.getCommittedAt() == null)
                .map(commitUdt -> {
                    if (!timeMap.containsKey(commitUdt.getCommitHash())) {
                        timeMap.put(commitUdt.getCommitHash(), movingDate.plusDays(dayIndex.getAndIncrement()).toInstant(ZoneOffset.UTC));
                    }
                    return commitUdt;
                }).toList();

        return timeMap;
    }

}
