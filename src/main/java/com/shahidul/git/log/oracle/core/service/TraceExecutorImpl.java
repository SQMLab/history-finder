package com.shahidul.git.log.oracle.core.service;

import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
        List<TraceEntity> traceEntityList = traceRepository.findAll().stream().limit(10).toList();
        traceServiceList.stream()
                .map(traceService -> {
                    traceEntityList.stream()
                            .filter(traceEntity -> traceEntity.getAnalysis().containsKey(traceService.getTracerName()) == false)
                            .map(traceEntity -> {
                                traceService.trace(traceEntity);
                                traceEntity = traceRepository.save(traceEntity);
                                return traceEntity;
                            }).toList();
                    return traceService;
                }).toList();
    }
}
