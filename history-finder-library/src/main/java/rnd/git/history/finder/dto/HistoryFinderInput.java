package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Getter;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @since 2/2/2024
 */
@Builder
@Getter
public class HistoryFinderInput {
    String cloneDirectory;
    String repositoryUrl;
    String repositoryName;
    String startCommitHash;
    LanguageType languageType;
    String file;
    String methodName;
    Integer startLine;
    String outputFile;
}
