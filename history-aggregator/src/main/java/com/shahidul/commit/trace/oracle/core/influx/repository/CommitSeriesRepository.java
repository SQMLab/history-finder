package com.shahidul.commit.trace.oracle.core.influx.repository;

import com.shahidul.commit.trace.oracle.core.influx.series.CommitSeries;

import java.util.List;

/**
 * @since 12/1/2023
 */
public interface CommitSeriesRepository {
    void deleteAll();
    List<CommitSeries> saveAll(List<CommitSeries> commitSeriesList);

    void deleteByOracleId(Integer oracleId);
}
