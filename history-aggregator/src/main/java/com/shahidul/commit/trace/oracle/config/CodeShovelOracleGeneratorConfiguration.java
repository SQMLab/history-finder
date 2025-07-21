package com.shahidul.commit.trace.oracle.config;

import com.shahidul.commit.trace.oracle.core.factory.YmlPropertySourceFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.List;

/**
 * @since 14/7/24
 **/
@Configuration
@ConfigurationProperties
@PropertySources({@PropertySource(value = "classpath:code-shovel-oracle-generator.yml", factory = YmlPropertySourceFactory.class)})
@Data
@NoArgsConstructor
public class CodeShovelOracleGeneratorConfiguration {
    List<String> repositoryList;
    Integer startOracleFileId;
    Integer maximumOraclePerRepository;
    String storedTraceDirectory;
    String headPointerMappingFile;
}
