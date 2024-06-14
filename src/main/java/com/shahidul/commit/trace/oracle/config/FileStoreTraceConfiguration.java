package com.shahidul.commit.trace.oracle.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shahidul Islam
 * @since 14/6/24
 **/
@Configuration
@ConditionalOnProperty(name = "trace.enable-mongodb", havingValue = "FALSE")
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class, org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class, org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration.class})
public class FileStoreTraceConfiguration {
}
