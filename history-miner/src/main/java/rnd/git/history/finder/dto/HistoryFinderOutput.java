package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
@Builder
@Getter
public class HistoryFinderOutput {
    List<Commit> commitList;
    List<HistoryEntry> historyEntryList;
    Long executionTime;
    Integer analyzedCommitCount;
    String methodId;
}
