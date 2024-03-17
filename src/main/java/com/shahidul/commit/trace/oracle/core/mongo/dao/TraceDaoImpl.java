package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/5/2024
 */
@Repository
@AllArgsConstructor
public class TraceDaoImpl implements TraceDao {
    TraceRepository traceRepository;
    @Override
    public List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer toFileId) {
        //TODO : Optimize
        return traceRepository.findAll()
                .stream().filter(traceEntity -> traceEntity.getOracleFileId() >= fromFileId && traceEntity.getOracleFileId() < toFileId)
                .sorted(Comparator.comparing(TraceEntity::getOracleFileId))
                .toList();
    }
}
