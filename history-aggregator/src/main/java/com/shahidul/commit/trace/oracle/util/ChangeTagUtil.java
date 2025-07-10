package com.shahidul.commit.trace.oracle.util;

import rnd.git.history.finder.dto.ChangeTag;

import java.util.*;

public class ChangeTagUtil {

    static Map<ChangeTag, String> TAG_TO_CODE_SHOVEL;

    static {
        TAG_TO_CODE_SHOVEL = new HashMap<>();
        TAG_TO_CODE_SHOVEL.put(ChangeTag.INTRODUCTION, "Yintroduced");
        //TAG_TO_CODE_SHOVEL.put(ChangeTag.SIGNATURE, "Ysignaturechange");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.RENAME, "Yrename");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.PARAMETER, "Yparameterchange");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.EXCEPTION, "Yexceptionschange");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.MODIFIER, "Ymodifierchange");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.BODY, "Ybodychange");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.MOVE, "Ymovefromfile");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.FILE_MOVE, "Yfilerename");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.DOCUMENTATION, "Ydocumentationchange");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.ANNOTATION, "Yannotationchnage");
        TAG_TO_CODE_SHOVEL.put(ChangeTag.FORMAT, "Yformatchange");
    }

    public static List<ChangeTag> toChangeTagsFromCodeShovel(String change) {
        Set<ChangeTag> changeTags = new HashSet<>();
        if (change != null) {
            if (change.contains("Yintroduced")) {
                changeTags.add(ChangeTag.INTRODUCTION);
            }
           /* if (change.contains("Ysignaturechange")) {
                changeTags.add(ChangeTag.SIGNATURE);
            }*/
            if (change.contains("Yrename")) {
                changeTags.add(ChangeTag.RENAME);
            }
            if (change.contains("Yreturntypechange")) {
                changeTags.add(ChangeTag.RETURN_TYPE);
            }
            if (change.contains("Yparameterchange")) {
                changeTags.add(ChangeTag.PARAMETER);
            }
            if (change.contains("Ymodifierchange")) {
                changeTags.add(ChangeTag.MODIFIER);
            }
            if (change.contains("Yexceptionschange")) {
                changeTags.add(ChangeTag.EXCEPTION);
            }

            if (change.contains("Ybodychange")) {
                changeTags.add(ChangeTag.BODY);
            }

            if (change.contains("Ymovefromfile")) {
                changeTags.add(ChangeTag.MOVE);
            }
            if (change.contains("Yfilerename")) {
                changeTags.add(ChangeTag.FILE_MOVE);
            }
        }
        List<ChangeTag> orderedTagList = new ArrayList<>(changeTags);
        orderedTagList.sort(ChangeTag.NATURAL_ORDER);
        return orderedTagList;
    }

    public static String toCodeShovelChangeText(List<ChangeTag> changeTagList) {
        List<String> codeShovelChangeTagList = new ArrayList<>();
        for (ChangeTag changeTag : changeTagList) {
            if (TAG_TO_CODE_SHOVEL.containsKey(changeTag)) {
                String codeShovelChangeTag = TAG_TO_CODE_SHOVEL.get(changeTag);
                if (!codeShovelChangeTagList.contains(codeShovelChangeTag)) {
                    codeShovelChangeTagList.add(codeShovelChangeTag);
                }
            }
        }
        if (codeShovelChangeTagList.isEmpty()) {
            return "";
        } else if (codeShovelChangeTagList.size() == 1) {
            return codeShovelChangeTagList.get(0);
        } else {
            return "Ymultichange(" + String.join(",", codeShovelChangeTagList) + ")";
        }
    }
}
