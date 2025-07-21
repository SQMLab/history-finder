package com.shahidul.commit.trace.oracle.core.influx;

import ch.qos.logback.core.util.TimeUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @since 12/1/2023
 */
@Configuration
@AllArgsConstructor
public class InfluxDbConfiguration {
    AppProperty appProperty;

    @Bean
    InfluxDBClient provideInfluxDbClient(){
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .readTimeout(Duration.ofMinutes(3))
                .writeTimeout(Duration.ofMinutes(3))
                .connectTimeout(Duration.ofSeconds(5));

        InfluxDBClientOptions influxDbClientOptions = InfluxDBClientOptions
                .builder()
                .url(appProperty.getUrl())
                .authenticateToken(appProperty.getToken().toCharArray())
                .bucket(appProperty.getBucketName())
                .org(appProperty.getOrganizationName())
                .okHttpClient(okHttpClient)
                .build();
        return InfluxDBClientFactory.create(influxDbClientOptions);
    }
}
