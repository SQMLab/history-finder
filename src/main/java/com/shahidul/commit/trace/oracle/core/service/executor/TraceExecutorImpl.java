package com.shahidul.commit.trace.oracle.core.service.executor;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.mongo.repository.TraceRepository;
import com.shahidul.commit.trace.oracle.core.service.algorithm.TraceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
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
    TraceRepository traceRepository;

    @Override
    public void execute() {
        List<TraceEntity> traceEntityList = traceRepository.findAll().stream().toList();
        StopWatch clock = new StopWatch();
        traceServiceList.stream()
                .map(traceService -> {
                    traceEntityList.stream()
                            .filter(traceEntity -> traceEntity.getAnalysis().containsKey(traceService.getTracerName()) == false)
                            .map(traceEntity -> {
                                clock.start();
                                traceService.trace(traceEntity);
                                clock.stop();
                                traceEntity.getAnalysis()
                                        .get(traceService.getTracerName())
                                        .setRuntime(clock.getLastTaskTimeMillis());
                                traceEntity = traceRepository.save(traceEntity);
                                return traceEntity;
                            }).toList();
                    return traceService;
                }).toList();
    }
}
