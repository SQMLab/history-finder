package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/5/2024
 */
public interface TraceDao {
    TraceEntity  findByOracleId(Integer oracleFileId);
    TraceEntity  findByOracleName(String oracleFileName);
    List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer toFileId);
    TraceEntity findByOracleHash(String oracleHash);
    CommitUdt findExpectedCommit(String oracleFileName, String commitHash);
    CommitUdt cloneStaticFields(CommitUdt commitUdt);
    void delete(TraceEntity traceEntity);
    TraceEntity save(TraceEntity traceEntity);
}
