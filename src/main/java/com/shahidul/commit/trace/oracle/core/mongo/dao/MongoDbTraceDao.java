package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.bson.BsonMaximumSizeExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 3/5/2024
 */
@Repository
@AllArgsConstructor
@ConditionalOnProperty(name = "trace.enable-mongodb", havingValue = "TRUE")
public class MongoDbTraceDao implements TraceDao {
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
    public List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer exclusiveToFileId) {
        //TODO : Optimize
        return traceRepository.findAll()
                .stream().filter(traceEntity -> traceEntity.getOracleFileId() >= fromFileId && traceEntity.getOracleFileId() < exclusiveToFileId)
                .sorted(Comparator.comparing(TraceEntity::getOracleFileId))
                .toList();
    }

    @Override
    public List<TraceEntity> findByOracleFileIdList(List<Integer> oracleFileIdList) {
        return traceRepository.findByOracleFileIdIn(oracleFileIdList);
    }

    @Override
    public TraceEntity findByOracleHash(String oracleHash) {
        return traceRepository.findByUid(oracleHash);
    }

    @Override
    public List<TraceEntity> findAll() {
        return traceRepository.findAll()
                .stream()
                .toList();
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

    @Override
    public List<TraceEntity> saveAll(List<TraceEntity> traceEntityList) {
        return traceRepository.saveAll(traceEntityList);
    }

    @Override
    public void deleteAll() {
        traceRepository.deleteAll();
    }

    private void quietlyTruncateDiff(TraceEntity traceEntity){
        traceEntity.getAnalysis().values().forEach(analysisUdt -> {

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
