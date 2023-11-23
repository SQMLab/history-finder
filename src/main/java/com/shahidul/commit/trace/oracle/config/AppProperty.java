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
}
