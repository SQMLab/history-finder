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
public enum TracerName {
    CODE_TRACKER("codeTracker"),
    CODE_SHOVEL("codeShovel"),
    GIT_LINE_RANGE("gitLineRange"),
    GIT_FUNC_NAME("gitFuncName"),
    HISTORY_FINDER("historyFinder"),
    INTELLI_J("intelliJ"),
    EXPECTED("expected"),
    AGGREGATED("aggregated");
    String code;
    public static TracerName fromCode(String code){
        for (TracerName tracerName : values()){
            if (tracerName.getCode().equals(code)){
                return tracerName;
            }
        }
        throw new RuntimeException("Illegal argument exception : " + code);
    }
    public static final List<TracerName> IMPLEMENTED = Arrays.asList(CODE_TRACKER, CODE_SHOVEL, GIT_LINE_RANGE, GIT_FUNC_NAME, HISTORY_FINDER);

    public static final List<TracerName> AGGREGATION_PRIORITY = Arrays.asList(CODE_SHOVEL, CODE_TRACKER, INTELLI_J, GIT_LINE_RANGE, GIT_FUNC_NAME);
}
