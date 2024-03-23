package com.shahidul.commit.trace.oracle.core.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.model.InputTrace;
import com.shahidul.commit.trace.oracle.core.model.StaticInputTrace;
import lombok.AllArgsConstructor;
import org.codetracker.api.MethodTracker;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Shahidul Islam
 * @since 3/23/2024
 */
@Repository
@AllArgsConstructor
public class StaticTraceDaoImpl implements StaticTraceDao {
    AppProperty appProperty;
    ObjectMapper objectMapper;

    @Override
    public StaticInputTrace findStaticTraceByOracleFileId(String oracleFileName) {
        File inputFile = Arrays.stream(new File(MethodTracker.class.getClassLoader().getResource(appProperty.getTraceFileDirectory())
                        .getFile())
                        .listFiles())
                .filter(traceFile -> traceFile.getName().equalsIgnoreCase(oracleFileName))
                .findFirst()
                .orElseThrow(() -> new CtoException(CtoError.Trace_Not_Found));
        try {
            return objectMapper.readValue(inputFile, StaticInputTrace.class);
        } catch (IOException e) {
            throw new CtoException(CtoError.File_Read_Error, e);
        }
    }

    @Override
    public InputTrace findTrace(String oracleFileName, TracerName tracerName) {
        InputTrace trace = findStaticTraceByOracleFileId(oracleFileName)
                .getTraceMap()
                .get(tracerName.getCode());
        if (trace == null){
            throw new CtoException(CtoError.Trace_Not_Found);
        }else {
            return trace;
        }
    }
}
