package com.shahidul.git.log.oracle.core.service.aggregator;

import com.shahidul.git.log.oracle.core.enums.TrackerName;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceAnalysisEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Service
@AllArgsConstructor
public class TraceAggregatorServiceImpl implements TraceAggregatorService {
    TraceRepository traceRepository;

    @Override
    @Transactional
    public void aggregate() {
        List<TraceEntity> traceEntityList = traceRepository.findAll().stream()
                .map(traceEntity -> {
                    List<CommitEntity> aggregatedList = traceEntity.getAnalysis()
                            .values()
                            .stream()
                            .map(TraceAnalysisEntity::getCommits)
                            .flatMap(List::stream)
                            .collect(Collectors.toMap(CommitEntity::getCommitHash, Function.identity(), (o1, o2) -> {
                                TrackerName trackerX = TrackerName.fromCode(o1.getTracerName());
                                TrackerName trackerY = TrackerName.fromCode(o2.getTracerName());
                                if( TrackerName.AGGREGATION_PRIORITY.indexOf(trackerX) <  TrackerName.AGGREGATION_PRIORITY.indexOf(trackerY)){
                                    return o1;
                                }else {
                                    return o2;
                                }
                            }))
                            .values()
                            .stream()
                            //TODO :  .sorted(Comparator.comparing(CommitEntity::getCommittedAt).reversed())
                            .toList();
                    traceEntity.setAggregatedCommits(aggregatedList);
                    return traceEntity;
                }).toList();
        traceRepository.saveAll(traceEntityList);
    }
}
