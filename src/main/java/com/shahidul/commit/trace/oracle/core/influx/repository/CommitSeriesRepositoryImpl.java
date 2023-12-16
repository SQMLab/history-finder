package com.shahidul.commit.trace.oracle.core.influx.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.domain.WritePrecision;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.influx.series.CommitSeries;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

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


}
