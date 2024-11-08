package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        return findTargetCommit(commitHash, fromTracer, traceEntity);
    }


    @Override
    public CommitUdt deleteCommit(String oracleFileName, String commitHash) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        List<CommitUdt> expectedCommits = traceEntity.getExpectedCommits();

        CommitUdt commitUdt = findCommit(expectedCommits, commitHash);
        expectedCommits.remove(commitUdt);
        traceDao.save(traceEntity);
        return commitUdt;
    }

    @Override
    public CommitUdt addCommit(String oracleFileName, String commitHash, TracerName fromTracer) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        try {
            CommitUdt commitInExpectedList = findCommit(traceEntity.getExpectedCommits(), commitHash);
        } catch (CtoException notFound) {
            if (CtoError.Commit_Not_Found.getCode().equals(notFound.getCode())){
                CommitUdt fromCommit = findCommit(traceEntity.getAnalysis().get(fromTracer.getCode()).getCommits(), commitHash);
                CommitUdt commit = traceDao.cloneStaticFields(fromCommit);
                commit.setTracerName(TracerName.EXPECTED.getCode());
                List<CommitUdt> expectedCommits = traceEntity.getExpectedCommits();
                int targetIndex = findInsertionIndex(expectedCommits, commit);
                expectedCommits.add(targetIndex, commit);
                traceDao.save(traceEntity);
                return commit;
            }
        }
        throw new CtoException(CtoError.Commit_Already_exist);
    }

    @Override
    public CommitUdt updateTags(String oracleFileName, String commitHash, TracerName fromTracer, List<ChangeTag> changeTagSet) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        CommitUdt targetCommit = findTargetCommit(commitHash, fromTracer, traceEntity);
        targetCommit.setChangeTags(changeTagSet);
        traceDao.save(traceEntity);
        return targetCommit;
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
        while (targetIndex < commitList.size() && targetCommit.getCommittedAt().before(commitList.get(targetIndex).getCommittedAt())) {
            targetIndex += 1;
        }
        return targetIndex;
    }

    private CommitUdt findTargetCommit(String commitHash, TracerName fromTracer, TraceEntity traceEntity) {
        List<CommitUdt> commits = null;
        if (fromTracer == TracerName.EXPECTED) {
            commits = traceEntity.getExpectedCommits();
        } else {
            commits = traceEntity.getAnalysis().get(fromTracer.getCode()).getCommits();
        }
        return findCommit(commits, commitHash);
    }
}
