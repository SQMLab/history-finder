package com.shahidul.commit.trace.oracle.core.enums;

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
    GIT_LINE_RANGE("gitLineRange"),
    GIT_FUNC_NAME("gitFuncName"),
    INTELLI_J("intelliJ");
    String code;
    public static TrackerName fromCode(String code){
        for (TrackerName trackerName : values()){
            if (trackerName.getCode().equals(code)){
                return trackerName;
            }
        }
        throw new RuntimeException("Illegal argument exception : " + code);
    }

    public static final List<TrackerName> AGGREGATION_PRIORITY = Arrays.asList(CODE_SHOVEL, CODE_TRACKER, INTELLI_J, GIT_LINE_RANGE, GIT_FUNC_NAME);
}
