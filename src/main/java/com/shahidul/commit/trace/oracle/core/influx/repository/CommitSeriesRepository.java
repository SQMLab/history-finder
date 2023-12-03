package com.shahidul.commit.trace.oracle.core.influx.repository;

import com.shahidul.commit.trace.oracle.core.influx.CommitSeriesEntity;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 12/1/2023
 */
public interface CommitSeriesRepository {
    void load();
    void deleteAll();
    List<CommitSeriesEntity> saveAll(List<CommitSeriesEntity> commitSeriesEntityList);
}
