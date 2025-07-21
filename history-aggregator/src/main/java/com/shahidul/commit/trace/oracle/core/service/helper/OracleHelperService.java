package com.shahidul.commit.trace.oracle.core.service.helper;

import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 5/1/2024
 */
public interface OracleHelperService {
    TraceEntity build(InputOracle inputOracle);

    String generateOracleHash(InputOracle inputOracle);
}
