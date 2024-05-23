package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.bson.BsonMaximumSizeExceededException;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

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
    public TraceEntity findByOracleHash(String oracleHash) {
        return traceRepository.findByUid(oracleHash);
    }

    @Override
    public CommitUdt findExpectedCommit(String oracleFileName, String commitHash) {
        TraceEntity traceEntity = findByOracleName(oracleFileName);
        List<CommitUdt> expectedCommits = traceEntity.getExpectedCommits();
        int targetIndex = 0;
        while (targetIndex < expectedCommits.size() && !expectedCommits.get(targetIndex).getCommitHash().startsWith(commitHash)) {
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
                .changeTags(commitUdt.getChangeTags())
                .newFile(commitUdt.getNewFile())
                .newElement(commitUdt.getNewElement())
                .author(commitUdt.getAuthor())
                .email(commitUdt.getEmail())
                .shortMessage(commitUdt.getShortMessage())
                .fullMessage(commitUdt.getFullMessage())
                .build();
    }

    @Override
    public void delete(TraceEntity traceEntity) {
        traceRepository.delete(traceEntity);

    }

    @Override
    public TraceEntity save(TraceEntity traceEntity) {
        try {
            return traceRepository.save(traceEntity);
        }catch (BsonMaximumSizeExceededException bsonMaximumSizeExceededException){
            quietlyTruncateDiff(traceEntity);
            traceEntity.setVersion(traceEntity.getVersion() - 1);
            return traceRepository.save(traceEntity);
        }
    }

    private void quietlyTruncateDiff(TraceEntity traceEntity){
        traceEntity.getAnalysis().values().stream().forEach(analysisUdt -> {

            truncateDiff(analysisUdt.getCommits());
            truncateDiff(analysisUdt.getCorrectCommits());
            truncateDiff(analysisUdt.getIncorrectCommits());
            truncateDiff(analysisUdt.getMissingCommits());
        });
    }

    private void truncateDiff(List<CommitUdt> commitUdtList){
        commitUdtList.forEach(commitUdt -> {
            truncateDiff(commitUdt);
        });
    }
    private void truncateDiff(CommitUdt commitUdt){
        commitUdt.setDiff(Util.truncate(commitUdt.getDiff(), 5000));
        commitUdt.setDiffDetail(Util.truncate(commitUdt.getDiffDetail(), 5000));
        commitUdt.setDocDiff(Util.truncate(commitUdt.getDocDiff(), 5000));
    }
}
