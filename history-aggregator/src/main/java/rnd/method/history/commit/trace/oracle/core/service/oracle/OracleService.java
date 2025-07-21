package rnd.method.history.commit.trace.oracle.core.service.oracle;

import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 3/26/2024
 */
public interface OracleService {
    TraceEntity deleteOracle(String oracleFileName);

}
