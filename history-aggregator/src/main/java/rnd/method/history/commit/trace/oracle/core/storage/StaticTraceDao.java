package rnd.method.history.commit.trace.oracle.core.storage;

import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.core.model.InputOracle;
import rnd.method.history.commit.trace.oracle.core.model.InputTrace;
import rnd.method.history.commit.trace.oracle.core.model.StaticInputTrace;

/**
 * @since 3/23/2024
 */
public interface StaticTraceDao {
    InputOracle save(InputOracle  inputOracle, String oracleFileName);
    StaticInputTrace findStaticTraceByOracleFileId(String oracleFileName);
    InputTrace findTrace(String oracleFileName, TracerName tracerName);

}
