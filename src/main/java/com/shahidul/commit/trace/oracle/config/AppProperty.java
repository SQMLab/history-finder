package com.shahidul.commit.trace.oracle.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Shahidul Islam
 * @since 11/12/2023
 */
@Component
@Getter
public class AppProperty {
    @Value("${tracer.repository.base-path}")
    String repositoryBasePath;
    @Value("${shell.command}")
    String gitShell;

    @Value("${influxdb.org-name}")
    String organizationName;
    @Value("${influxdb.token}")
    String token;
    @Value("${influxdb.url}")
    String url;
    @Value("${influxdb.bucket-name}")
    String bucketName;
}
