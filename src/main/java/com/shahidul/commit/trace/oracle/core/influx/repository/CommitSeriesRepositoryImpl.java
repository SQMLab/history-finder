package com.shahidul.commit.trace.oracle.core.influx.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.influx.CommitSeriesEntity;
import com.shahidul.commit.trace.oracle.core.influx.InfluxDbConfiguration;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 12/1/2023
 */
@Repository
@AllArgsConstructor
public class CommitSeriesRepositoryImpl implements CommitSeriesRepository {
    InfluxDBClient influxDBClient;
    AppProperty appProperty;
    TraceRepository  traceRepository;
    @Override
    public void load() {
        deleteAll();
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
                    allCommitList.addAll(traceEntity.getAggregatedCommits());
                    saveAll(allCommitList.stream().map(commitUdt -> toCommitSeries(traceEntity, commitUdt)).toList());
                    return traceEntity;
                }).toList();

    }

    @Override
    public void deleteAll() {
        DeletePredicateRequest predicateRequest =  new DeletePredicateRequest();
        predicateRequest.setStart(OffsetDateTime.now().minusYears(20));
        predicateRequest.setStart(OffsetDateTime.now());
        influxDBClient.getDeleteApi()
                .delete( OffsetDateTime.now().minusYears(20), OffsetDateTime.now(),"_measurement=\"commit\"", appProperty.getBucketName(), appProperty.getOrganizationName());
    }

    @Override
    public List<CommitSeriesEntity> saveAll(List<CommitSeriesEntity> commitSeriesEntityList) {
        influxDBClient.getWriteApiBlocking()
                .writeMeasurements(WritePrecision.S, commitSeriesEntityList);

    /*    commitSeriesEntityList.forEach(commit -> {
            influxDBClient.getWriteApiBlocking()
                    .writePoint(Point.measurement("commit")
                            .addTag("tracerName", commit.getTracerName())
                            .addField("commitHash", commit.getCommitHash())
                                    .time(commit.getCommittedAt(), WritePrecision.S));
        });*/
        return commitSeriesEntityList;
    }

    private CommitSeriesEntity toCommitSeries(TraceEntity traceEntity, CommitUdt commitUdt){
        return CommitSeriesEntity.builder()
                .oracleFileId(traceEntity.getOracleFileId())
                .oracleFileName(traceEntity.getOracleFileName())
                .tracerName(commitUdt.getTracerName())
                .commitHash(commitUdt.getCommitHash().substring(0, 4))
                .committedAt(commitUdt.getCommittedAt().toInstant())
                .build();
    }
}
