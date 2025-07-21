package com.shahidul.commit.trace.oracle.cmd.model;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import lombok.Builder;
import lombok.Getter;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @since 2/2/2024
 */
@Builder
@Getter
public class CommandLineInput {
    String command;
    String cloneDirectory;
    TracerName tracerName;
    Integer oracleFileId;
    String repositoryUrl;
    String repositoryName;
    String startCommitHash;
    LanguageType languageType;
    String file;
    String methodName;
    Integer startLine;
    Integer endLine;
    String outputFile;
}
