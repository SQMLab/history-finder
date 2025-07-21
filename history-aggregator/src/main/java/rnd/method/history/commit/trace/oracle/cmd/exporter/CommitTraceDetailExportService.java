package rnd.method.history.commit.trace.oracle.cmd.exporter;

import rnd.method.history.commit.trace.oracle.cmd.model.CommandLineInput;
import rnd.git.history.finder.dto.CommitTraceOutput;

/**
 * @since 5/1/2024
 */
public interface CommitTraceDetailExportService {
    void export(CommandLineInput commandLineInput);
    CommitTraceOutput execute(CommandLineInput commandLineInput, boolean useCache);
}
