package rnd.method.history.commit.trace.oracle.cmd.exporter;

import rnd.method.history.commit.trace.oracle.cmd.model.CommandLineInput;

/**
 * @since 5/1/2024
 */
public interface CommitTraceComparisonExportService {
    void export(CommandLineInput commandLineInput);
}
