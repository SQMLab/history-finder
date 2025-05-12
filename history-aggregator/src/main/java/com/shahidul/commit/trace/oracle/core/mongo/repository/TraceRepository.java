package com.shahidul.commit.trace.oracle.core.mongo.repository;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Repository
public interface TraceRepository extends MongoRepository<TraceEntity, String> {
    TraceEntity findByOracleFileId(Integer oracleFileId);
    List<TraceEntity> findAllByOracleFileId(Integer oracleFileId);
    TraceEntity findByOracleFileName(String oracleFileName);
    TraceEntity findTopByUid(String oracleHash);
    List<TraceEntity> findByOracleFileIdBetween(Integer fromOracleId, Integer toOracleId);
    List<TraceEntity> findByOracleFileIdBetween(Range<Integer> oracleFileIdRange);
    List<TraceEntity> findByOracleFileIdIn(List<Integer> oracleFileIdList);
    List<TraceEntity> findByOracleFileIdInOrderByOracleFileId(List<Integer> oracleFileIdList);
}
