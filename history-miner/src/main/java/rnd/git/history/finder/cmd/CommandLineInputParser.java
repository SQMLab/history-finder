package rnd.git.history.finder.cmd;

import rnd.git.history.finder.dto.HistoryFinderInput;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
public interface CommandLineInputParser {
    HistoryFinderInput parse(String [] args);
}
