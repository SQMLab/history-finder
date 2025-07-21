package rnd.method.history.commit.trace.oracle.core.service.loader;

import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;

import java.util.List;

/**
 * @since 11/11/2023
 */
public interface DataSetLoader {
    TraceEntity loadOracleFile(Integer oracleFileId );
    List<TraceEntity> loadFile(int limit);
    void preProcessCodeShoveFile();

    void processIntelliJInputDump();

    void cleanDb();

    void updateExpectedCommit(List<TraceEntity> traceEntityList, TracerName fromTracer);

    List<TraceEntity> updateCommitChangeTag();
}
