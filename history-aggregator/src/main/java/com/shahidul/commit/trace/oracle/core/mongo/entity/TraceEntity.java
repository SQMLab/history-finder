package com.shahidul.commit.trace.oracle.core.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import rnd.git.history.finder.enums.LanguageType;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("trace")
public class TraceEntity {
    @Id
    @Field("_id")
    ObjectId id;
    @Field
    String uid;
    @Field
    @Indexed
    Integer oracleFileId;
    @Field
    String cloneDirectory;
    @Field
    String oracleFileName;
    @Field
    String repositoryName;
    @Field
    String repositoryUrl;
    @Field
    String file;
    @Field
    LanguageType languageType;
    @Field
    String elementType;
    @Field
    String startCommitHash;
    @Field
    String elementName;
    @Field
    String methodId;
    @Field
    Integer startLine;
    @Field
    Integer endLine;
    @Field
    Double precision;
    @Field
    Double recall;
    @Field
    @Version
    Integer version;
    @Field
    @CreatedDate
    Date createdAt;
    @Field
    @LastModifiedDate
    Date updatedAt;
    @Field
    List<CommitUdt> expectedCommits;
    @Field
    Map<String, AnalysisUdt> analysis;
}
