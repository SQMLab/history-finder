package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class Commit {
    String commitHash;
    String parentCommitHash;
    String methodCode;
    String documentation;
    String annotation;
    String methodContainerFile;
    Set<ChangeTag> changeTags;
    CommitInfo commitInfo;// so that we only make changes here when we update it
    Integer startLine;
    Integer endLine;
}
