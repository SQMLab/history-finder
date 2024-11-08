package com.shahidul.commit.trace.oracle.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Shahidul Islam
 * @since 12/23/2023
 */
@AllArgsConstructor
@Getter
public enum ChangeTag {
    INTRODUCTION("introduction"),
    MOVE("move"),
    BODY("body"),
    REMOVE("remove"),
    DOCUMENTATION("documentation"),
    FILE_MOVE("file_move"),
    RENAME("rename"),
    MODIFIER("modifier"),
    RETURN_TYPE("return_type"),
    EXCEPTION("exception"),
    PARAMETER("parameter"),
    ANNOTATION("annotation"),
    FORMAT("format"),
    //TODO : remove
    PACKAGE("package"),
    SIGNATURE("signature"),
    FILE_RENAME("file_rename"),
    FILE_COPY("file_copy"),
    ACCESS_MODIFIER("access_modifier")
    ;
    String code;

    public static ChangeTag fromTag(String tag) {
        if (tag == null) {
            return null;
        } else {
            for (ChangeTag changeTag : values()) {
                if (tag.equalsIgnoreCase(changeTag.getCode())) {
                    return changeTag;
                }
            }
        }
        throw new RuntimeException("Tag name mapping not listed " + tag);
    }
}
