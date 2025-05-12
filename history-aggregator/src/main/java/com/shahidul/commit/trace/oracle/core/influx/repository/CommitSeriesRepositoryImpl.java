package com.shahidul.commit.trace.oracle.core.influx.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.domain.WritePrecision;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.influx.series.CommitSeries;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 12/1/2023
 */
@Repository
@AllArgsConstructor
public class CommitSeriesRepositoryImpl implements CommitSeriesRepository {
    InfluxDBClient influxDBClient;
    AppProperty appProperty;


    @Override
    public void deleteAll() {
        influxDBClient.getInfluxQLQueryApi()
                .query(new InfluxQLQuery("DROP MEASUREMENT commit", "cto"));
    }

    @Override
    public List<CommitSeries> saveAll(List<CommitSeries> commitSeriesList) {
        influxDBClient.getWriteApiBlocking()
                .writeMeasurements(WritePrecision.S, commitSeriesList);
        return commitSeriesList;
    }

    @Override
    public void deleteByOracleId(Integer oracleId) {
        DeletePredicateRequest predicateRequest =  new DeletePredicateRequest();
        predicateRequest.setStart(OffsetDateTime.now().minusYears(20));
        predicateRequest.setStart(OffsetDateTime.now());
        influxDBClient.getDeleteApi()
                .delete( OffsetDateTime.now().minusYears(20), OffsetDateTime.now(),"_measurement=\"commit\" and oracle_file_id=" + oracleId, appProperty.getBucketName(), appProperty.getOrganizationName());
    }

}
