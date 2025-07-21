package com.shahidul.commit.trace.oracle.config;

import com.shahidul.commit.trace.oracle.core.factory.YmlPropertySourceFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.Map;

/**
 * @since 14/7/24
 **/
@Configuration
@ConfigurationProperties
@PropertySources({@PropertySource(value = "classpath:repository-mapping.yml", factory = YmlPropertySourceFactory.class)})
@Data
@NoArgsConstructor
public class RepositoryNameUrlMappingConfiguration {
    Map<String, String> repositoryMapping;
}
