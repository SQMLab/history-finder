package com.shahidul.commit.trace.oracle.cmd.parser;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;

/**
 * @since 2/2/2024
 */
public interface CommandLineInputParser {
    CommandLineInput parse(String [] args);
}
