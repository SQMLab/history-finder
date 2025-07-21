package rnd.method.history.commit.trace.oracle.core.service.helper;

import rnd.method.history.commit.trace.oracle.core.model.InputOracle;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 5/1/2024
 */
public interface OracleHelperService {
    TraceEntity build(InputOracle inputOracle);

    String generateOracleHash(InputOracle inputOracle);
}
