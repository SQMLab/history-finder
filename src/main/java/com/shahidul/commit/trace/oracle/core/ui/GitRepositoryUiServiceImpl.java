package com.shahidul.commit.trace.oracle.core.ui;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class GitRepositoryUiServiceImpl implements GitRepositoryUiService {
    AppProperty appProperty;

    @Override
    public List<String> findRepositoryList() {
        File cloneDirectory = new File(appProperty.getRepositoryBasePath());
        return Arrays.stream(Objects.requireNonNull(cloneDirectory.list())).sorted(Comparator.naturalOrder()).toList();

    }

    @Override
    public List<String> findPathList(String repositoryName, String commitHash, String path) {
        //TODO : checkout commit
        StringBuilder pathBuilder = new StringBuilder(appProperty.getRepositoryBasePath())
                .append("/").append(repositoryName);
        if (!path.isEmpty()) {
            pathBuilder.append("/").append(path);
        }
        File file = new File(pathBuilder.toString());
        String[] fileList = new String[]{};
        if (file.exists()){
            if (file.isFile()){
                fileList = new String[]{file.getAbsolutePath()};
            }else{
                fileList = file.list((dir,fileName)-> dir.isDirectory() || fileName.endsWith(".java"));
            }
        }
        return Arrays.stream(fileList)
                .map(f-> f.substring(pathBuilder.length()))
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
