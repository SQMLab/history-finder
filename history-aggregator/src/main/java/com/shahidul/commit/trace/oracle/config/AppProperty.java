package com.shahidul.commit.trace.oracle.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rnd.git.history.finder.Util;

/**
 * @since 11/12/2023
 */
@Component
@Getter
public class AppProperty {
    @Value("${server.port}")
    Integer serverPort;
    @Value("${repository.clone-directory}")
    String repositoryBasePath;
    @Value("${cmd.shell}")
    String gitShell;

    @Value("${influxdb.org-name}")
    String organizationName;
    @Value("${influxdb.token}")
    String token;
    @Value("${influxdb.url}")
    String url;
    @Value("${influxdb.bucket-name}")
    String bucketName;
    @Value("${run-config.execution-limit}")
    Integer executionLimit;
    @Value("${oracle.file-directory}")
    String oracleFileDirectory;
    @Value("${trace.file-directory}")
    String traceFileDirectory;
    @Value("${trace.cache-directory}")
    String traceCacheDirectory;
    @Value("${trace.cache-directory-clean-start:false}")
    Boolean traceCacheDirectoryCleanStart;

    @PostConstruct
    public void expandPaths() {
        this.repositoryBasePath = Util.expandPath(repositoryBasePath);
        this.oracleFileDirectory = Util.expandPath(oracleFileDirectory);
        this.traceFileDirectory = Util.expandPath(traceFileDirectory);
        this.traceCacheDirectory = Util.expandPath(traceCacheDirectory);
    }
}

