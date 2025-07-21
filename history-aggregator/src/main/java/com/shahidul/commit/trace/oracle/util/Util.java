package com.shahidul.commit.trace.oracle.util;

import com.felixgrund.codeshovel.util.Utl;
import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import org.apache.http.util.TextUtils;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @since 12/20/2023
 */
public class Util {
    public static String formatOracleFileId(int oracleFileId) {
        return String.format("%03d", oracleFileId);
    }
    public static String findRepositoryName(String url){
        String urlWithoutDotGit;
        if (url.toLowerCase().endsWith(".git")){
            urlWithoutDotGit = url.substring(0, url.length() - ".git".length());
        }else {
            urlWithoutDotGit = url;
        }
        return extractLastPart(urlWithoutDotGit);
    }
    public static String getRepoUrlFromLocalPath(String path){
        GitService gitService = new GitServiceImpl();
        try (Repository repository = gitService.cloneIfNotExists(path, null)) {
            return repository.getConfig().getString("remote", "origin", "url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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


    public static String extractClassNameFromFile(String file){
        String[] fileParts = file.split("/");
        String lastPart = fileParts[fileParts.length - 1];
        if (lastPart.contains(".")){
            return lastPart.substring(0, lastPart.indexOf("."));
        }else {
            return lastPart;
        }
    }
    public static Integer extractOracleFileId(String oracleFileName){
        return Integer.parseInt(oracleFileName.split("-")[0]);
    }

    public static String extractLastPart(String file){
        return rnd.git.history.finder.Util.extractLastPart(file);
    }
    public static String concatPath(String prefix, String suffix){
        if (prefix.endsWith("/")){
            return prefix + suffix;
        }else{
            return prefix + "/" + suffix;
        }

    }

    public static String getLocalProjectDirectory(String baseDirectory, String defaultBaseDirectory, String projectName){
        if (TextUtils.isEmpty(baseDirectory)){
            return concatPath(defaultBaseDirectory,  projectName);
        }else {
            return concatPath(baseDirectory, projectName);
        }
    }
}
