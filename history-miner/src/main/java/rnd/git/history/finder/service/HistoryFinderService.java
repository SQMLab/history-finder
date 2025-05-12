package rnd.git.history.finder.service;

import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.dto.HistoryFinderOutput;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
public interface HistoryFinderService {
    HistoryFinderOutput findSync(HistoryFinderInput input);

}
