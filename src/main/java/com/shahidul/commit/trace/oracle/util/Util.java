package com.shahidul.commit.trace.oracle.util;

import com.felixgrund.codeshovel.util.Utl;
import org.eclipse.jgit.diff.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Shahidul Islam
 * @since 12/20/2023
 */
public class Util {
    public static String formatOracleFileId(int oracleFileId) {
        return String.format("%03d", oracleFileId);
    }

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

    public static String readLineRange(String fileContent, Integer startLine, Integer endLine) {
       /* StringBuilder textBuilder = new StringBuilder();
        String[] lines = fileContent.split("\n");
        for (int i = startLine - 1; i < endLine; i++){
            textBuilder.append(lines[i]);
            if (i + 1< endLine){
                textBuilder.append('\n');
            }
        }
        return textBuilder.toString();*/
        return Utl.getTextFragment(fileContent, startLine, endLine);
    }

    public static String getDiff(String oldText, String newText) {
    /*    if (oldText == null){
            return newText;
        }else if (newText ==  null){
            return oldText;
        }*/
        try {
            RawText sourceOld = new RawText((oldText == null ? "" : oldText).getBytes());
            RawText sourceNew = new RawText((newText == null ? "" : newText).getBytes());
            DiffAlgorithm diffAlgorithm = new HistogramDiff();
            RawTextComparator textComparator = RawTextComparator.DEFAULT;
            EditList editList = diffAlgorithm.diff(textComparator, sourceOld, sourceNew);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(out);
            formatter.setContext(1000);
            formatter.format(editList, sourceOld, sourceNew);
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static String truncate(String text, int limit) {
        if (text == null || text.length() <= limit) {
            return text;
        } else {
            return text.substring(0, limit);
        }
    }

    public static List<Integer> parseOraceFileIds(String fileIdsText) {
        Set<Integer> idSet = new HashSet<>();
        String[] idOrRanges = fileIdsText.replace(" ", "").split(",");
        for (int i = 0, idOrRangesLength = idOrRanges.length; i < idOrRangesLength; i++) {
            String idOrRange = idOrRanges[i];
            if (idOrRange.contains("-")) {
                String[] range = idOrRange.split("-");
                int[] ids = IntStream.rangeClosed(Integer.parseInt(range[0]), Integer.parseInt(range[1]))
                        .toArray();
                for (int id : ids) {
                    idSet.add(id);
                }
            } else {
                idSet.add(Integer.parseInt(idOrRange));
            }
        }
        return idSet.stream()
                .sorted()
                .toList();
    }
}
