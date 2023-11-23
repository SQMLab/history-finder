package com.shahidul.commit.trace.oracle.core.mongo.repository;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Repository
public interface TraceRepository extends MongoRepository<TraceEntity, String> {
}
