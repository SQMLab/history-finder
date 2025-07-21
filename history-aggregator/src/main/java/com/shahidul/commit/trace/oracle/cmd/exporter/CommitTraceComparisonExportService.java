package com.shahidul.commit.trace.oracle.cmd.exporter;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;

/**
 * @since 5/1/2024
 */
public interface CommitTraceComparisonExportService {
    void export(CommandLineInput commandLineInput);
}
