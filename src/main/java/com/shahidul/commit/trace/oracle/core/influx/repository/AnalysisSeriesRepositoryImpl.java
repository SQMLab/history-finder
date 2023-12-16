package com.shahidul.commit.trace.oracle.core.influx.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.domain.WritePrecision;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.influx.series.AnalysisSeries;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 12/3/2023
 */
@Repository
@AllArgsConstructor
public class AnalysisSeriesRepositoryImpl implements AnalysisSeriesRepository {
    InfluxDBClient influxDBClient;
    AppProperty appProperty;
    @Override
    public void deleteAll() {
        influxDBClient.getInfluxQLQueryApi()
                .query(new InfluxQLQuery("DROP MEASUREMENT analysis", "cto"));}

    @Override
    public List<AnalysisSeries> saveAll(List<AnalysisSeries> analysisSeriesList) {
        influxDBClient.getWriteApiBlocking()
                .writeMeasurements(WritePrecision.S, analysisSeriesList);
        return analysisSeriesList;
    }
}
