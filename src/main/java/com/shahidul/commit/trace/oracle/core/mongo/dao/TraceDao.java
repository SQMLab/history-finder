package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/5/2024
 */
public interface TraceDao {
    List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer toFileId);
}
