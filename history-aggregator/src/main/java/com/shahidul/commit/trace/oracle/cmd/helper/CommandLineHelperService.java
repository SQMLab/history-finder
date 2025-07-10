package com.shahidul.commit.trace.oracle.cmd.helper;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import rnd.git.history.finder.dto.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
public interface CommandLineHelperService {
    InputOracle toInputOracle(CommandLineInput commandLineInput);
    TraceEntity loadOracle(InputOracle inputOracle, Integer optionalOracleFileId, String cloneDirectory);
    CommitTraceOutput readOutput(TraceEntity traceEntity, TracerName tracerName);
}
