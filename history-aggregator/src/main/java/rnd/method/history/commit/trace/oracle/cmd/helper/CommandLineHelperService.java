package rnd.method.history.commit.trace.oracle.cmd.helper;

import rnd.method.history.commit.trace.oracle.cmd.model.CommandLineInput;
import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.core.model.InputOracle;
import rnd.git.history.finder.dto.CommitTraceOutput;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @since 19/5/24
 **/
public interface CommandLineHelperService {
    InputOracle toInputOracle(CommandLineInput commandLineInput);
    TraceEntity loadOracle(InputOracle inputOracle, Integer optionalOracleFileId, String cloneDirectory, boolean useCache);
    CommitTraceOutput readOutput(TraceEntity traceEntity, TracerName tracerName);
}
