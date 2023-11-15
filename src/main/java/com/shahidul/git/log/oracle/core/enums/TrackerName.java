package com.shahidul.git.log.oracle.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Getter
@AllArgsConstructor
public enum TrackerName {
    CODE_TRACKER("codeTracker"),
    CODE_SHOVEL("codeShovel"),
    GIT_REGEX("gitRegex"),
    JET_BRAINS("jetBrains");
    String code;
    public static TrackerName fromCode(String code){
        for (TrackerName trackerName : values()){
            if (trackerName.getCode().equals(code)){
                return trackerName;
            }
        }
        throw new RuntimeException("Illegal argument exception : " + code);
    }

    public static final List<TrackerName> AGGREGATION_PRIORITY = Arrays.asList(CODE_SHOVEL, CODE_TRACKER, GIT_REGEX, JET_BRAINS);
}
