package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/19/2024
 */
@AllArgsConstructor
@Service
public class ExpectedCommitServiceImpl implements ExpectedCommitService {
    TraceDao traceDao;

    @Override
    public CommitUdt findCommit(String oracleFileName, String commitHash, TracerName fromTracer) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        List<CommitUdt> commits = null;
        if (fromTracer == TracerName.EXPECTED) {
            commits = traceEntity.getExpectedCommits();
        }else {
            commits = traceEntity.getAnalysis().get(fromTracer.getCode()).getCommits();
        }
        return findCommit(commits, commitHash);
    }

    @Override
    public CommitUdt deleteCommit(String oracleFileName, String commitHash) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        List<CommitUdt> expectedCommits = traceEntity.getExpectedCommits();
        int targetIndex = 0;

        CommitUdt commitUdt = findCommit(expectedCommits, commitHash);
        CommitUdt previousCommit = targetIndex - 1 >= 0 ? expectedCommits.get(targetIndex - 1) : null;
        CommitUdt nextCommit = targetIndex + 1 < expectedCommits.size() ? expectedCommits.get(targetIndex + 1) : null;
        if (nextCommit != null) {
            if (previousCommit != null) {
                nextCommit.setParentCommitHash(previousCommit.getCommitHash());
                nextCommit.setOldFile(previousCommit.getNewFile());
                nextCommit.setOldElement(previousCommit.getNewElement());
                //TODO : nextCommit.setFileRenamed();
                //TODO : nextCommit.setDiff();
                //TODO : nextCommit.setFileMoved();
            } else {
                //TODO : handle as introduced commit
            }
        }

        return expectedCommits.get(targetIndex);

    }

    @Override
    public CommitUdt addCommit(String oracleFileName, String commitHash, TracerName fromTracer) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        CommitUdt commit = traceDao.cloneStaticFields(findCommit(traceEntity.getAnalysis().get(fromTracer.getCode()).getCommits(), commitHash));
        commit.setTracerName(TracerName.EXPECTED.getCode());
        List<CommitUdt> expectedCommits = traceEntity.getExpectedCommits();
        int targetIndex = findInsertionIndex(expectedCommits, commit);
        CommitUdt previousCommit = targetIndex - 1 >= 0 ? expectedCommits.get(targetIndex - 1) : null;
        CommitUdt nextCommit = targetIndex < expectedCommits.size() ? expectedCommits.get(targetIndex) : null;

        if (previousCommit != null) {
            commit.setParentCommitHash(previousCommit.getCommitHash());
        }
        if (nextCommit != null) {
            nextCommit.setParentCommitHash(commit.getCommitHash());
        }
        //TODO : Update previous, commit, next commit properties


        return commit;
    }

    private CommitUdt findCommit(List<CommitUdt> commitList, String commitHash) {
        int targetIndex = 0;
        while (targetIndex < commitList.size() && !commitList.get(targetIndex).getCommitHash().startsWith(commitHash)) {
            targetIndex += 1;
        }
        if (targetIndex < commitList.size()) {
            return commitList.get(targetIndex);
        } else throw new CtoException(CtoError.Commit_Not_Found);
    }

    private int findInsertionIndex(List<CommitUdt> commitList, CommitUdt targetCommit) {
        int targetIndex = 0;
        while (targetIndex < commitList.size() && targetCommit.getCommittedAt().after(commitList.get(targetIndex).getCommittedAt())) {
            targetIndex += 1;
        }
        return targetIndex;
    }
}
