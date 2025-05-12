package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class HistoryEntry {
    MethodHolder oldMethodHolder;
    MethodHolder newMethodHolder;
    String ancestorCommitHash;
    Set<ChangeTag> changeTagSet;
}
