package com.shahidul.commit.trace.oracle.cmd.exporter;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;

/**
 * @author Shahidul Islam
 * @since 5/1/2024
 */
public interface CommitTraceDetailExportService {
    void export(CommandLineInput commandLineInput);
    CommitTraceOutput execute(CommandLineInput commandLineInput);
}
