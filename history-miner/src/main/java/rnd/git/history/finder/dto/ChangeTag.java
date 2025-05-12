package rnd.git.history.finder.dto;

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
    FORMAT("format");
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
