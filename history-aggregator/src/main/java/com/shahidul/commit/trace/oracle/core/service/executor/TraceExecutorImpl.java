package com.shahidul.commit.trace.oracle.core.service.executor;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/12/2023
 */
@Service
@AllArgsConstructor
public class TraceExecutorImpl implements TraceExecutor {
    List<TraceService> traceServiceList;
    TraceDao traceDao;
    AppProperty appProperty;

    @Override
    public void execute() {
        List<TraceEntity> traceEntityList = traceDao.findAll();
        traceServiceList.stream()
                .map(traceService -> {
                    traceEntityList.stream()
                            .filter(traceEntity -> traceEntity.getAnalysis().containsKey(traceService.getTracerName()) == false)
                            .map(traceEntity -> execute(traceEntity, traceService))
                            .toList();
                    return traceService;
                }).toList();
    }

    @Override
    @Transactional
    public TraceEntity execute(TraceEntity traceEntity, TraceService traceService) {

        Repository repository = null;
        try {
            repository = new GitServiceImpl().cloneIfNotExists(Util.getLocalProjectDirectory(traceEntity.getCloneDirectory(), appProperty.getRepositoryBasePath(), traceEntity.getRepositoryName()), traceEntity.getRepositoryUrl());
        } catch (Exception e) {
            throw new CtoException(CtoError.Git_Checkout_Failed, e);
        }
        repository.close();
        StopWatch clock = new StopWatch();
        clock.start();
        traceService.trace(traceEntity);
        clock.stop();
        traceEntity.getAnalysis()
                .get(traceService.getTracerName())
                .setRuntime(clock.getLastTaskTimeMillis());
        return traceDao.save(traceEntity);
    }
}
