package com.shahidul.commit.trace.oracle.core.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.shahidul.commit.trace.oracle.api.payload.RepositoryListResponse;
import com.shahidul.commit.trace.oracle.cmd.exporter.CommitTraceDetailExportService;
import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.model.HistoryInputParam;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.CommitTraceOutput;
import rnd.git.history.finder.dto.InputOracle;
import rnd.git.history.finder.enums.LanguageType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class GitRepositoryUiServiceImpl implements GitRepositoryUiService {
    private final ObjectMapper objectMapper;
    AppProperty appProperty;
    CommitTraceDetailExportService traceDetailExportService;

    @Override
    public RepositoryListResponse findRepositoryList() {
        File cloneDirectory = new File(appProperty.getRepositoryBasePath());
        String[] repoArrays = cloneDirectory.list();
        List<String> repositoryList = Arrays.asList(repoArrays != null ? repoArrays : new String[0]);
        repositoryList.sort(String.CASE_INSENSITIVE_ORDER);
        return RepositoryListResponse.builder()
                .repositoryPath(appProperty.getRepositoryBasePath())
                .repositoryList(repositoryList)
                .build();
    }

    @Override
    public List<String> findPathList(String repositoryPath, String repositoryName, String startCommitHash, String path) {
        GitService gitService = new GitServiceImpl();
        try (Repository repository = gitService.cloneIfNotExists(repositoryPath + "/" + repositoryName, "")) {
            gitService.checkout(repository, startCommitHash);
        } catch (Exception e) {
            throw new CtoException(CtoError.Git_Checkout_Failed, e);
        }

        StringBuilder pathBuilder = new StringBuilder(repositoryPath)
                .append("/").append(repositoryName);
        if (!path.isEmpty()) {
            pathBuilder.append("/").append(path);
        }
        File file = new File(pathBuilder.toString());
        if (file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
            return List.of(path);
        } else {
            List<String> files = buildCompactTree(file, "");
            return files.stream()
                    .map(f -> path.isEmpty() ? f : path + "/" + f)
                    .toList();
        }

    }

    @Override
    public List<MethodLocationDto> findMethodLocationList(String repositoryPath,
                                                          String repositoryName,
                                                          String commitHash,
                                                          String file) {
        List<MethodDeclaration> methodDeclarationList = parseAllMethods(repositoryPath, repositoryName, commitHash, file);
        return methodDeclarationList.stream()
                .map(dec -> MethodLocationDto.builder()
                        .methodName(dec.getSignature().getName())
                        .signature(dec.getDeclarationAsString())
                        .startLine(dec.getName().getBegin().get().line) //dec.getBegin() includes annotations
                        .endLine(dec.getEnd().get().line)
                        .build())
                .sorted(Comparator.comparing(MethodLocationDto::getMethodName, String.CASE_INSENSITIVE_ORDER))
                .toList();

    }

    @Override
    public CommitTraceOutput findMethodHistory(String repositoryHostName,
                                               String repositoryAccountName,
                                               String repositoryPath,
                                               String repositoryName,
                                               String commitHash,
                                               String file,
                                               String methodName,
                                               Integer startLine,
                                               Integer endLine,
                                               TracerName tracerName,
                                               boolean forceExecute) {
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
        return traceDetailExportService.execute(inputCommand, forceExecute);
    }

    @Override
    public RepositoryCheckoutResponse checkoutRepository(String location) {
        RepositoryCheckoutResponse checkoutInfo = parseRepository(location);
        GitService gitService = new GitServiceImpl();
        String repositoryUrl = null;
        if (checkoutInfo.getHost() != null && checkoutInfo.getAccountName() != null) {
            repositoryUrl = checkoutInfo.getHost() + "/" + checkoutInfo.getAccountName() + "/" + checkoutInfo.getRepositoryName() + ".git";
        }
        try (Repository repository = gitService.cloneIfNotExists(checkoutInfo.getPath() + "/" + checkoutInfo.getRepositoryName(), repositoryUrl)) {
            if (checkoutInfo.getHost() == null) {
                String localOriginUrl = repository.getConfig().getString("remote", "origin", "url");
                log.info(localOriginUrl);
                if (localOriginUrl != null) {
                    RepositoryCheckoutResponse locallyStoredRepositoryInfo = parseRepository(localOriginUrl);
                    if (checkoutInfo.getHost() == null || checkoutInfo.getHost().isEmpty()) {
                        checkoutInfo.setHost(locallyStoredRepositoryInfo.getHost());
                    }
                    if (checkoutInfo.getAccountName() == null || checkoutInfo.getAccountName().isEmpty()) {
                        checkoutInfo.setAccountName(locallyStoredRepositoryInfo.getAccountName());
                    }
                } else {
                    log.warn("Remote not configured");
                }
            }
            return checkoutInfo;
        } catch (Exception e) {
            throw new CtoException(CtoError.Failed_To_Find_Repositories, e);
        }
    }

    @Override
    public List<String> getOracleFileList() {

        File folder = new File(appProperty.getOracleFileDirectory()); // Replace with your actual directory path


        // Regex for any digits followed by a hyphen (e.g., 1-, 001-, 123456-)
        Pattern pattern = Pattern.compile("^\\d+-");

        List<File> matchingFiles = new ArrayList<>();
        listMatchingFilesRecursively(folder, pattern, matchingFiles);

        List<String> fileNames = new ArrayList<>();
        for (File file : matchingFiles) {
            fileNames.add(file.getName());
        }
        fileNames.sort(Comparator.naturalOrder());
        return fileNames;
//        return List.of("001-checkstyle-Checker-fireErrors.json");
    }

    @Override
    public HistoryInputParam findOracleMethodHistory(String fileName, TracerName tracerName) {

        File file = findFile(new File(appProperty.getOracleFileDirectory()), fileName);
        try {
            InputOracle inputOracle = objectMapper.readValue(file, InputOracle.class);
            RepositoryCheckoutResponse repositoryInfo = checkoutRepository(inputOracle.getRepositoryUrl());
            return HistoryInputParam.builder()
                    .repositoryName(repositoryInfo.getRepositoryName())
                    .startCommitHash(inputOracle.getStartCommitHash())
                    .file(inputOracle.getFile())
                    .methodName(inputOracle.getElement())
                    .startLine(inputOracle.getStartLine())
                    .endLine(inputOracle.getEndLine())
                    .repositoryPath(repositoryInfo.getPath())
                    .repositoryUrl(inputOracle.getRepositoryUrl())
                    .repositoryHostName(repositoryInfo.getHost())
                    .repositoryAccountName(repositoryInfo.getAccountName())
                    .oracleFileId(Util.extractOracleFileId(fileName))
                    .tracerName(tracerName)
                    .build();

        } catch (IOException e) {
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
                    .repositoryName(parts[2].replace(".git", "")) // TODO: repository name is not always the folder name of project it might be different
                    .path(appProperty.getRepositoryBasePath())
                    .build();
        }
        // Check if input matches an SSH VCS URL
        else if (input.matches(sshPattern)) {
            String[] parts = input.replaceFirst(sshPattern, "$1,$2,$3").split(",");
            return RepositoryCheckoutResponse.builder()
                    .host("https://" + parts[0])
                    .accountName(parts[1])
                    .repositoryName(parts[2].replace(".git", ""))
                    .path(appProperty.getRepositoryBasePath())
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
                        .path(input.substring(0, lastSlash))
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

    private List<MethodDeclaration> parseAllMethods(String repositoryPath,
                                                    String repositoryName,
                                                    String commitHash,
                                                    String file) {
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
                .orElseThrow(() -> new CtoException(CtoError.Java_Method_Parsing_Failed));
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
            } else if (file.getName().toLowerCase().endsWith(".java")) {
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

    private static void listMatchingFilesRecursively(File dir, Pattern pattern, List<File> result) {
        if (dir == null || !dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                listMatchingFilesRecursively(file, pattern, result);
            } else if (pattern.matcher(file.getName()).find()) {
                result.add(file);
            }
        }
    }

    private static File findFile(File dir, String targetFileName) {

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File targetFile = findFile(file, targetFileName);
                    if (targetFile != null) {
                        return targetFile;
                    }
                } else if (targetFileName.equalsIgnoreCase(file.getName())) {
                    return file;
                }
            }
        }
        return null;
    }
}
