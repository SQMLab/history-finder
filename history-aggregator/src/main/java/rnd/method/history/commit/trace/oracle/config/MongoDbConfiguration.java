package rnd.method.history.commit.trace.oracle.config;

import rnd.method.history.commit.trace.oracle.core.mongo.MongoDbRootPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @since 11/10/2023
 */
@Configuration
@ConditionalOnProperty(name = "trace.enable-mongodb", havingValue = "TRUE")
@EnableMongoRepositories(basePackageClasses = MongoDbRootPackage.class)
@EnableMongoAuditing
public class MongoDbConfiguration {
}
