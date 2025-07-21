package rnd.method.history.commit.trace.oracle.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rnd.git.history.finder.dto.InputCommit;

import java.util.List;

/**
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputOracle {
    String repositoryName;
    String repositoryUrl;
    String startCommitHash;
    String file;
    String language;
    String elementType;
    String element;
    Integer startLine;
    Integer endLine;
    List<InputCommit> commits;
}
