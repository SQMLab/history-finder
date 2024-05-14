package com.shahidul.commit.trace.oracle.cmd;

import rnd.git.history.finder.dto.HistoryFinderInput;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
public interface CtoCommandLineInputParser {
    CtoCmdInput parse(String [] args);
}
