package com.shahidul.commit.trace.oracle.core.service.loader;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

import java.util.List;

/**
 * @author Shahidul Islam
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
