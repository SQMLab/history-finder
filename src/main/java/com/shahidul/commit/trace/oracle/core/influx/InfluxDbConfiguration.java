package com.shahidul.commit.trace.oracle.core.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shahidul Islam
 * @since 12/1/2023
 */
@Configuration
@AllArgsConstructor
public class InfluxDbConfiguration {
    AppProperty appProperty;

    @Bean
    InfluxDBClient provideInfluxDbClient(){
        return InfluxDBClientFactory.create(appProperty.getUrl(), appProperty.getToken().toCharArray(), appProperty.getOrganizationName(), appProperty.getBucketName());
    }
}
