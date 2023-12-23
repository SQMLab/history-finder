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
    INTRODUCE("introduce"),
    PACKAGE("package"),
    MOVE("move"),
    BODY("body"),
    SIGNATURE("signature"),
    REMOVE("remove"),
    DOCUMENTATION("documentation"),
    FILE_RENAME("file_rename"),
    RENAME("rename"),
    MODIFIER("modifier"),
    ACCESS_MODIFIER("access_modifier"),
    RETURN_TYPE("return_type"),
    EXCEPTION("exception"),
    PARAMETER("parameter"),
    ANNOTATION("annotation");
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
