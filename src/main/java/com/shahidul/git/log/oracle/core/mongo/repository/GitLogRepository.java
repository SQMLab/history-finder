package com.shahidul.git.log.oracle.core.mongo.repository;

import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Repository
public interface GitLogRepository extends MongoRepository<GitLogEntity, String> {
}
