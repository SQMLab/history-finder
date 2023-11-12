package com.shahidul.git.log.oracle.config;

import com.shahidul.git.log.oracle.core.mongo.MongoDbRootPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Configuration
@EnableMongoRepositories(basePackageClasses = MongoDbRootPackage.class)
@EnableMongoAuditing
public class MongoDbConfiguration {
}
