package com.shahidul.git.log.oracle.core.model;

import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Data
@Builder
public class LogTracerOutput {
    List<GitLogEntity> gitLogList;
}
