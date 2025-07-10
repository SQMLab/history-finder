package rnd.git.history.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputOracle {
    String oracleFileName;
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
