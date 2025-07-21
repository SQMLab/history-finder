package rnd.method.history.commit.trace.oracle.cmd.parser;

import rnd.method.history.commit.trace.oracle.cmd.model.CommandLineInput;

/**
 * @since 2/2/2024
 */
public interface CommandLineInputParser {
    CommandLineInput parse(String [] args);
}
