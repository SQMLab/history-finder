package com.shahidul.commit.trace.oracle.core.service.loader;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @author Shahidul Islam
 * @since 11/11/2023
 */
public interface DataSetLoader {
    TraceEntity loadOracleFile(Integer oracleFileId );
    void loadFile(int limit);
    void preProcessCodeShoveFile();
    void cleanDb();
}
