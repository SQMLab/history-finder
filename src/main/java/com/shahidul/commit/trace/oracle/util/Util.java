package com.shahidul.commit.trace.oracle.util;

import com.felixgrund.codeshovel.util.Utl;
import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import org.eclipse.jgit.diff.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public static String getDiffUrl(String repositoryUrl, String parentCommitHash, String commitHash, String file) {
        String fileSection = "";
        if (file != null){
            fileSection =  "#diff-" + sha256(file.getBytes(StandardCharsets.UTF_8)).toLowerCase();
        }
        return repositoryUrl.replaceAll("\\.git", "") + "/compare/" + parentCommitHash + "..." + commitHash  + fileSection;
    }

    public static String gitRawFileUrl(String repositoryUrl, String commitHash, String file, Integer lineNumber){
        return repositoryUrl.replaceAll("\\.git", "") + "/blob/" + commitHash + "/" + file + (lineNumber != null ? "#L" + lineNumber : "");
    }
    public static String getCommitUrl(String repositoryUrl, String commitHash){
        return repositoryUrl.replaceAll("\\.git", "") + "/commit/" + commitHash;
    }
    public static String getUserSearchUrl(String authorName){
        return "https://github.com/search?q=" + authorName + "&type=Users";
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
        if (fileContent != null) {
            return Utl.getTextFragment(fileContent, startLine, endLine);
        }else {
            return null;
        }
    }

    public static String getDiff(String oldText, String newText) {
    /*    if (oldText == null){
            return newText;
        }else if (newText ==  null){
            return oldText;
        }*/
        try {
            RawText sourceOld = new RawText((oldText == null ? "" : oldText).getBytes(StandardCharsets.UTF_8));
            RawText sourceNew = new RawText((newText == null ? "" : newText).getBytes(StandardCharsets.UTF_8));
            DiffAlgorithm diffAlgorithm = new HistogramDiff();
            RawTextComparator textComparator = RawTextComparator.DEFAULT;
            EditList editList = diffAlgorithm.diff(textComparator, sourceOld, sourceNew);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(out);
            formatter.setContext(1000);
            formatter.format(editList, sourceOld, sourceNew);
            return out.toString(StandardCharsets.UTF_8);
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

    public static List<Integer> parseOracleFileIds(String fileIdsText) {
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
    @SneakyThrows
    public static String sha256(byte[] content) {
        MessageDigest crypt = MessageDigest.getInstance("SHA-256");
        crypt.reset();
        crypt.update(content);
        return DatatypeConverter.printHexBinary(crypt.digest());
    }

    public static String extractClassNameFromFile(String file){
        String[] fileParts = file.split("/");
        String lastPart = fileParts[fileParts.length - 1];
        if (lastPart.contains(".")){
            return lastPart.substring(0, lastPart.indexOf("."));
        }else {
            return lastPart;
        }
    }

    public static String extractFileName(String file){
        String[] fileParts = file.split("/");
        return fileParts[fileParts.length - 1];
    }

}
