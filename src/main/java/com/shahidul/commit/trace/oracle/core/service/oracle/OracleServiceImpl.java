package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.influx.InfluxDbManager;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Shahidul Islam
 * @since 3/26/2024
 */
@Service
@AllArgsConstructor
public class OracleServiceImpl implements OracleService {
    TraceDao traceDao;
    InfluxDbManager influxDbManager;

    @Override
    public TraceEntity deleteOracle(String oracleFileName) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        traceDao.delete(traceEntity);
        influxDbManager.deleteByFileId(traceEntity.getOracleFileId());
        return traceEntity;
    }
}

