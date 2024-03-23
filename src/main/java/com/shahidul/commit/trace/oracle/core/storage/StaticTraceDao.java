package com.shahidul.commit.trace.oracle.core.storage;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.InputTrace;
import com.shahidul.commit.trace.oracle.core.model.StaticInputTrace;

/**
 * @author Shahidul Islam
 * @since 3/23/2024
 */
public interface StaticTraceDao {
    InputOracle save(InputOracle  inputOracle, String oracleFileName);
    StaticInputTrace findStaticTraceByOracleFileId(String oracleFileName);
    InputTrace findTrace(String oracleFileName, TracerName tracerName);

}
