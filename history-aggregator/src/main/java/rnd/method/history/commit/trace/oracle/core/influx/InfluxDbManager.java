package rnd.method.history.commit.trace.oracle.core.influx;

import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 12/3/2023
 */
public interface InfluxDbManager {
    TraceEntity load(TraceEntity traceEntity);
    void deleteCommitSeriesByFileId(Integer fileId);
    void deleteAnalysisSeriesByFileId(Integer fileId);
    void deleteByFileId(Integer oracleFileId);
    void deleteAll();

}
