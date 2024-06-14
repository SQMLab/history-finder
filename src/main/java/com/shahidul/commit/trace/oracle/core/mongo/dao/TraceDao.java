package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/5/2024
 */
public interface TraceDao {
    TraceEntity findByOracleId(Integer oracleFileId);

    TraceEntity findByOracleName(String oracleFileName);

    List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer exclusiveToFileId);
    List<TraceEntity> findByOracleFileIdList(List<Integer> oracleFileIdList);

    TraceEntity findByOracleHash(String oracleHash);

    List<TraceEntity> findAll();

    void delete(TraceEntity traceEntity);

    TraceEntity save(TraceEntity traceEntity);
    List<TraceEntity> saveAll(List<TraceEntity>  traceEntityList);
    void deleteAll();
    default CommitUdt cloneStaticFields(CommitUdt commitUdt) {
        return CommitUdt.builder()
                .commitHash(commitUdt.getCommitHash())
                .committedAt(commitUdt.getCommittedAt())
                .startLine(commitUdt.getStartLine())
                .endLine(commitUdt.getEndLine())
                .codeFragment(commitUdt.getCodeFragment())
                .changeTags(commitUdt.getChangeTags())
                .newFile(commitUdt.getNewFile())
                .newElement(commitUdt.getNewElement())
                .author(commitUdt.getAuthor())
                .email(commitUdt.getEmail())
                .shortMessage(commitUdt.getShortMessage())
                .fullMessage(commitUdt.getFullMessage())
                .build();
    }
}
