package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @author Shahidul Islam
 * @since 3/22/2024
 */
public interface OracleDumpService {
    TraceEntity dumpOracle(Integer oracleFileName);
}
