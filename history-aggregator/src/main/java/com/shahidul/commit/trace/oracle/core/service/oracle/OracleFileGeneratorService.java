package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 3/22/2024
 */
public interface OracleFileGeneratorService {
    InputOracle generateFile(String oracleFileName);
}
