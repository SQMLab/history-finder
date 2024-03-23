package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Shahidul Islam
 * @since 3/5/2024
 */
@Repository
@AllArgsConstructor
public class TraceDaoImpl implements TraceDao {
    TraceRepository traceRepository;

    @Override
    public TraceEntity findByOracleId(Integer oracleFileId) {

        return traceRepository.findByOracleFileId(oracleFileId);
    }

    @Override
    public TraceEntity findByOracleName(String oracleFileName) {
        return traceRepository.findByOracleFileName(oracleFileName);
    }

    @Override
    public List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer toFileId) {
        //TODO : Optimize
        return traceRepository.findAll()
                .stream().filter(traceEntity -> traceEntity.getOracleFileId() >= fromFileId && traceEntity.getOracleFileId() < toFileId)
                .sorted(Comparator.comparing(TraceEntity::getOracleFileId))
                .toList();
    }

    @Override
    public CommitUdt findExpectedCommit(String oracleFileName, String commitHash) {
        TraceEntity traceEntity = findByOracleName(oracleFileName);
        List<CommitUdt> expectedCommits = traceEntity.getExpectedCommits();
        int targetIndex = 0;
        while (targetIndex < expectedCommits.size() && !commitHash.equalsIgnoreCase(expectedCommits.get(targetIndex).getCommitHash())) {
            targetIndex += 1;
        }
        if (targetIndex < expectedCommits.size()) {
            return expectedCommits.get(targetIndex);
        } else throw new CtoException(CtoError.Commit_Not_Found);
    }

    @Override
    public CommitUdt cloneStaticFields(CommitUdt commitUdt) {
        return CommitUdt.builder()
                .commitHash(commitUdt.getCommitHash())
                .committedAt(commitUdt.getCommittedAt())
                .startLine(commitUdt.getStartLine())
                .endLine(commitUdt.getEndLine())
                .codeFragment(commitUdt.getCodeFragment())
                .changeTags(new TreeSet<>())
                .newFile(commitUdt.getNewFile())
                .newElement(commitUdt.getNewElement())
                .author(commitUdt.getAuthor())
                .email(commitUdt.getEmail())
                .shortMessage(commitUdt.getShortMessage())
                .fullMessage(commitUdt.getFullMessage())
                .build();
    }
}
