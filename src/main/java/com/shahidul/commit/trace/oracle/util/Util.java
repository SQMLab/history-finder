package com.shahidul.commit.trace.oracle.util;

/**
 * @author Shahidul Islam
 * @since 12/20/2023
 */
public class Util {
    public static boolean isFileRenamed(String oldFile, String newFile) {
        int oldFileNameStartIndex = Math.max(oldFile.lastIndexOf("/"), 0);
        int newFileNameStartIndex = Math.max(newFile.lastIndexOf("/"), 0);
        return !oldFile.substring(oldFileNameStartIndex).equals(newFile.substring(newFileNameStartIndex));
    }

    public static boolean isFileMoved(String oldFile, String newFile) {
        int oldFileNameStartIndex = Math.max(oldFile.lastIndexOf("/"), 0);
        int newFileNameStartIndex = Math.max(newFile.lastIndexOf("/"), 0);
        return !oldFile.substring(0, oldFileNameStartIndex + 1).equals(newFile.substring(0, newFileNameStartIndex + 1));
    }

    public static String getDiffUrl(String repositoryUrl, String parentCommitHash, String commitHash) {
        return repositoryUrl.replaceAll("\\.git", "") + "/compare/" + parentCommitHash + "..." + commitHash;
    }
}
