package rnd.git.history.finder.service;

import rnd.git.history.finder.dto.CommitTraceOutput;
import rnd.git.history.finder.dto.HistoryFinderInput;

/**
 * @since 2/2/2024
 */
public interface HistoryFinderService {
    CommitTraceOutput findSync(HistoryFinderInput input);

}
