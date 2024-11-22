package com.shahidul.commit.trace.oracle.core.ui;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.shahidul.commit.trace.oracle.cmd.exporter.CommitTraceDetailExportService;
import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.enums.LanguageType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class GitRepositoryUiServiceImpl implements GitRepositoryUiService {
    AppProperty appProperty;
    CommitTraceDetailExportService traceDetailExportService;

    @Override
    public List<String> findRepositoryList() {
        File cloneDirectory = new File(appProperty.getRepositoryBasePath());
        String[] repoArrays = cloneDirectory.list();
        List<String> repositoryList = Arrays.asList(repoArrays != null ? repoArrays : new String[0]);
        repositoryList.sort(String.CASE_INSENSITIVE_ORDER);
        return repositoryList;

    }

    @Override
    public List<String> findPathList(String repositoryPath, String repositoryName, String commitHash, String path) {
        //TODO : checkout commit
        StringBuilder pathBuilder = new StringBuilder(repositoryPath)
                .append("/").append(repositoryName);
        if (!path.isEmpty()) {
            pathBuilder.append("/").append(path);
        }
        File file = new File(pathBuilder.toString());
      /*  if (file.exists()) {
            if (file.isFile()) {
                return List.of(file.getName());
            } else {
                return Arrays.stream(Objects.requireNonNull(file.listFiles(f -> {
                    if (f.isDirectory()) {
                        return !f.getName().startsWith(".");
                    } else {
                        return f.getName().endsWith(".java");
                    }
                }))).map(f -> {
                   *//* if (f.isDirectory()) {
                        return f.getName() + "/";
                    }else{
                        return f.getName();
                    }*//*
                    return path.isEmpty() ? f.getName() : path + "/" + f.getName();
                }).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
            }
        } else {
            return Collections.emptyList();
        }*/
        List<String> files = buildCompactTree(file, "");
        return files.stream()
                .map(f -> path.isEmpty() ? f : path + "/" + f)
                .toList();
    }

    @Override
    public List<MethodLocationDto> findMethodLocationList(String repositoryPath, String repositoryName, String commitHash, String file) {
        List<MethodDeclaration> methodDeclarationList = parseAllMethods(repositoryPath, repositoryName, commitHash, file);
        return methodDeclarationList.stream()
                .map(dec -> MethodLocationDto.builder()
                        .methodName(dec.getSignature().getName())
                        .signature(dec.getDeclarationAsString())
                        .startLine(dec.getBegin().get().line)
                        .endLine(dec.getEnd().get().line)
                        .build())
                .sorted(Comparator.comparing(MethodLocationDto::getMethodName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public CommitTraceOutput findMethodHistory(String repositoryHostName, String repositoryAccountName, String repositoryPath, String repositoryName, String commitHash, String file, String methodName, Integer startLine, Integer endLine, TracerName tracerName) {
        CommandLineInput inputCommand = CommandLineInput.builder()
                .command("")
                .tracerName(tracerName)
                .oracleFileId(null)
                .cloneDirectory(repositoryPath)
                .repositoryUrl(repositoryHostName + "/" + repositoryAccountName + "/" + repositoryName + ".git")
                .repositoryName(repositoryName)
                .startCommitHash(commitHash)
                .languageType(LanguageType.JAVA)
                .file(file)
                .methodName(methodName)
                .startLine(startLine)
                .endLine(endLine)
                .build();
        return traceDetailExportService.execute(inputCommand);
    }

    @Override
    public RepositoryCheckoutResponse checkoutRepository(String location) {
        RepositoryCheckoutResponse checkoutInfo = parseRepository(location);
        GitService gitService = new GitServiceImpl();
        String repositoryUrl = null;
        if (checkoutInfo.getHost() != null && checkoutInfo.getAccountName() != null) {
            repositoryUrl = checkoutInfo.getHost() + "/" + checkoutInfo.getAccountName() + "/" + checkoutInfo.getRepositoryName() + ".git";
        }
        try (Repository repository = gitService.cloneIfNotExists(checkoutInfo.getPath() + "/" + checkoutInfo.getRepositoryName(), repositoryUrl)){
            if (checkoutInfo.getHost() == null){
                String localOriginUrl = repository.getConfig().getString("remote", "origin", "url");
                log.info(localOriginUrl);
                if (localOriginUrl != null) {
                    String[] parts = localOriginUrl.replaceFirst("^(https?://[^/]+)/([^/]+)/([^/]+)(\\.git)?$", "$1,$2,$3").split(",");
                    checkoutInfo.setHost(parts[0]);
                    checkoutInfo.setAccountName(parts[1]);
                }else {
                    log.warn("Remote not configured");
                }
            }
            return checkoutInfo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RepositoryCheckoutResponse parseRepository(String input) {

        // Patterns for parsing VCS URLs
        String httpPattern = "^(https?://[^/]+)/([^/]+)/([^/]+)(\\.git)?$";
        String sshPattern = "^git@([^:]+):([^/]+)/([^/]+)(\\.git)?$";
        String ghPattern = "^gh repo clone ([^/]+)/([^/]+)$";

        // Check if input matches an HTTP(S) VCS URL
        if (input.matches(httpPattern)) {
            String[] parts = input.replaceFirst(httpPattern, "$1,$2,$3").split(",");
            return RepositoryCheckoutResponse.builder()
                    .host(parts[0])
                    .accountName(parts[1])
                    .repositoryName(parts[2].replace(".git", ""))
                    .path(appProperty.getRepositoryBasePath())
                    .build();
        }
        // Check if input matches an SSH VCS URL
        else if (input.matches(sshPattern)) {
            String[] parts = input.replaceFirst(sshPattern, "$1,$2,$3").split(",");
            return RepositoryCheckoutResponse.builder()
                    .host("https://"+parts[0])
                    .accountName(parts[1])
                    .repositoryName(parts[2].replace(".git", ""))
                    .path(appProperty.getRepositoryBasePath() )
                    .build();
        }
        // Check if input matches GitHub CLI format
        else if (input.matches(ghPattern)) {
            String[] parts = input.replaceFirst(ghPattern, "$1,$2").split(",");
            return RepositoryCheckoutResponse.builder()
                    .host("https://github.com")
                    .accountName(parts[0])
                    .repositoryName(parts[1])
                    .path(appProperty.getRepositoryBasePath())
                    .build();
        }
        // Check if input is a local path
        else {
            // Normalize input path
            input = input.trim();
            if (input.endsWith("/")) {
                input = input.substring(0, input.length() - 1);
            }
            int lastSlash = input.lastIndexOf('/');
            if (lastSlash != -1) {
                return RepositoryCheckoutResponse.builder()
                        .path(input.substring(0,lastSlash))
                        .repositoryName(input.substring(lastSlash + 1))
                        .build();
            } else {
                return RepositoryCheckoutResponse.builder()
                        .path(appProperty.getRepositoryBasePath())
                        .repositoryName(input)
                        .build();
            }
        }
    }

    private List<MethodDeclaration> parseAllMethods(String repositoryPath, String repositoryName, String commitHash, String file) {
        File javaFile = new File(repositoryPath + "/" + repositoryName + "/" + file);
        try {
            String fileContent = FileUtils.readFileToString(javaFile, StandardCharsets.UTF_8);
            CompilationUnit compilationUnit = parseCompilationUnit(fileContent);
            return compilationUnit.findAll(MethodDeclaration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CompilationUnit parseCompilationUnit(String sourceCode) {
        return new JavaParser()
                .parse(sourceCode).getResult()
                .orElseThrow(() -> new RuntimeException("Failed to to parse code as compilation unit"));
    }

    private static List<String> buildCompactTree(File dir, String prefix) {
        List<String> result = new ArrayList<>();
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            return result;
        }

        // Separate directories and .java files
        List<File> directories = new ArrayList<>();
        List<File> javaFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                // Filter out directories that start with '.' or have no .java files in their subtree
                if (!file.getName().startsWith(".") && containsJavaFilesInSubtree(file)) {
                    directories.add(file);
                }
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }

        // Add directories to the result, compacting if necessary
        for (File directory : directories) {
            String compactedPath = compactDirectory(directory, prefix.isEmpty() ? directory.getName() : prefix + "/" + directory.getName());
            result.add(compactedPath);
        }

        // Add .java files only if they are at the root of the current package
        for (File file : javaFiles) {
            result.add((prefix.isEmpty() ? "" : prefix + "/") + file.getName());
        }

        return result;
    }

    private static String compactDirectory(File directory, String prefix) {
        File current = directory;
        String compactedPath = prefix;

        while (true) {
            File[] files = current.listFiles();
            if (files == null) {
                break;
            }

            // Separate directories and .java files
            List<File> subDirectories = new ArrayList<>();
            List<File> javaFiles = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    // Filter out directories starting with '.' or without .java files in their subtree
                    if (!file.getName().startsWith(".") && containsJavaFilesInSubtree(file)) {
                        subDirectories.add(file);
                    }
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }

            // Stop compaction if there are multiple subdirectories or Java files
            if (javaFiles.size() > 0 || subDirectories.size() != 1) {
                break;
            }

            // Continue compacting into the single subdirectory
            current = subDirectories.get(0);
            compactedPath += "/" + current.getName();
        }

        return compactedPath;
    }

    private static boolean containsJavaFilesInSubtree(File dir) {
        File[] files = dir.listFiles();

        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively check subdirectories
                if (containsJavaFilesInSubtree(file)) {
                    return true;
                }
            } else if (file.getName().endsWith(".java")) {
                return true; // Found a .java file
            }
        }

        return false; // No .java files in this directory or its subtree
    }

}
