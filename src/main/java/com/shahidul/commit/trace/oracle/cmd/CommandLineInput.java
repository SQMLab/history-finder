package com.shahidul.commit.trace.oracle.cmd;

import lombok.Builder;
import lombok.Getter;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
@Builder
@Getter
public class CommandLineInput {
    String cacheDirectory;
    String repositoryUrl;
    String repositoryName;
    String startCommitHash;
    LanguageType languageType;
    String file;
    String methodName;
    Integer startLine;
    String outputFile;
}
