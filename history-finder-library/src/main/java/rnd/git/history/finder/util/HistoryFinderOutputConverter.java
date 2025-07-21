package rnd.git.history.finder.util;

import rnd.git.history.finder.dto.CommitTraceOutput;
import rnd.git.history.finder.dto.HistoryEntry;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.jgit.JgitService;

import java.util.List;

/**
 * @since 2025-07-10
 */
public interface HistoryFinderOutputConverter {
    CommitTraceOutput convert(JgitService jgitService, HistoryFinderInput historyFinderInput, List<HistoryEntry> historyEntryList, Long executionTime, Integer analyzedCommitCount, String methodId);
}
