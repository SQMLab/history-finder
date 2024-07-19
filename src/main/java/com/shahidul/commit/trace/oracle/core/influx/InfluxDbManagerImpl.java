package com.shahidul.commit.trace.oracle.core.influx;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.influx.repository.AnalysisSeriesRepository;
import com.shahidul.commit.trace.oracle.core.influx.repository.CommitSeriesRepository;
import com.shahidul.commit.trace.oracle.core.influx.series.AnalysisSeries;
import com.shahidul.commit.trace.oracle.core.influx.series.CommitSeries;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
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
    AnalysisSeriesRepository analysisSeriesRepository;
    CommitSeriesRepository commitSeriesRepository;

    @Override
    public TraceEntity load(TraceEntity traceEntity) {

        LocalDateTime analysisSeriesDateTime = LocalDate.ofYearDay(2023, traceEntity.getOracleFileId()).atStartOfDay();


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
        List<CommitSeries> updatedCommitSeries = allCommitList.stream().map(commitUdt -> toCommitSeries(traceEntity, commitUdt, timeMap)).toList();
        commitSeriesRepository.saveAll(updatedCommitSeries);


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
    }

    @Override
    public void deleteCommitSeriesByFileId(Integer fileId) {
        commitSeriesRepository.deleteByOracleId(fileId);
    }

    @Override
    public void deleteAnalysisSeriesByFileId(Integer fileId) {
        analysisSeriesRepository.deleteByOracleId(fileId);

    }

    @Override
    public void deleteByFileId(Integer oracleFileId) {
        commitSeriesRepository.deleteByOracleId(oracleFileId);
        analysisSeriesRepository.deleteByOracleId(oracleFileId);
    }

    @Override
    public void deleteAll() {
        commitSeriesRepository.deleteAll();
        analysisSeriesRepository.deleteAll();
    }

    private CommitSeries toCommitSeries(TraceEntity traceEntity, CommitUdt commitUdt, Map<String, Instant> timeMap) {
        return CommitSeries.builder()
                .oracleFileId(traceEntity.getOracleFileId())
                .oracleFileName(traceEntity.getOracleFileName())
                .tracerName(commitUdt.getTracerName())
                .commitHash(commitUdt.getCommitHash().substring(0, 4))
                .committedAt(commitUdt.getCommittedAt() == null ? Instant.now() : commitUdt.getCommittedAt().toInstant())
                .startLine(commitUdt.getStartLine())
                .endLine(commitUdt.getEndLine())
                .oldFile(commitUdt.getOldFile())
                .newFile(commitUdt.getNewFile())
                .fileRenamed(commitUdt.getFileRenamed())
                .fileMoved(commitUdt.getFileMoved())
                .oldElement(commitUdt.getOldElement())
                .newElement(commitUdt.getNewElement())
                .changeTagSet(commitUdt.getChangeTags())
                .diffUrl(commitUdt.getDiffUrl())
                .oldFileUrl(commitUdt.getOldFilUrl())
                .newFileUrl(commitUdt.getNewFileUrl())
                .diff(commitUdt.getDiff())
                .docDiff(commitUdt.getDocDiff())
                /*          .diff(Util.truncate(commitUdt.getDiff(), 100))
                          .docDiff(Util.truncate(commitUdt.getDocDiff(), 100))*/
                .diffDetail(commitUdt.getDiffDetail())
                .translatedAt(timeMap.get(commitUdt.getCommitHash()))
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
                .translatedAt(createdAt)
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
