package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
public class CommitNode {
    String commitHash;
    @Setter
    List<String> ancestorCommitHashes;
}
