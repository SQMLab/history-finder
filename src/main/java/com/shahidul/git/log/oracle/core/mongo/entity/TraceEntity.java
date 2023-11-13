package com.shahidul.git.log.oracle.core.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    String uri; // repositoryName || blob || commitHash ||filePath || #startLine No
    @Field
    String repositoryName;
    @Field
    String repositoryUrl;
    @Field
    String commitHash;
    @Field
    String filePath;
    @Field
    String functionName;
    @Field
    String functionKey;
    @Field Integer startLine;
    List<CommitEntity> expectedCommits;
    @Field
    List<CommitEntity> aggregatedCommits;
    @Field
    Map<String, TraceAnalysisEntity> analysis;
    @Field
    @Version
    Integer version;
    @Field
    Date createdAt;

    @Field
    Date updatedAt;
}
