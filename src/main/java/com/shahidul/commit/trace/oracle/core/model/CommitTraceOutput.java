package com.shahidul.commit.trace.oracle.core.model;

import com.shahidul.commit.trace.oracle.core.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommitTraceOutput {
    String repositoryName;
    String repositoryUrl;
    String startCommitHash;
    String file;
    LanguageType language;
    String elementType;
    String element;
    Integer startLine;
    Integer endLine;

    Double precision;
    Double recall;
    Long runtime;

    List<String> commits;
    List<OutputCommitDetail> commitDetails;
}
