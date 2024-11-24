package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.ui.GitRepositoryUiService;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@AllArgsConstructor
public class MethodHistoryUiController {
    GitRepositoryUiService gitRepositoryUiService;

    @GetMapping("ui/method-selector")
    public String showMethodSelectorUi() {
        return "method-selector";
    }

    @GetMapping("ui/method-history")
    public String showMethodHistoryUi(@RequestParam("repositoryHostName") String repositoryHostName,
                                      @RequestParam("repositoryAccountName") String repositoryAccountName,
                                      @RequestParam("repositoryPath") String repositoryPath,
                                      @RequestParam("repositoryName") String repositoryName,
                                      @RequestParam("startCommitHash") String startCommitHash,
                                      @RequestParam("file") String file,
                                      @RequestParam("methodName") String methodName,
                                      @RequestParam("startLine") Integer startLine,
                                      @RequestParam("endLine") Integer endLine,
                                      @RequestParam("tracerName") TracerName tracerName,
                                      Model model) {
        CommitTraceOutput traceOutput = gitRepositoryUiService.findMethodHistory(repositoryHostName,
                repositoryAccountName,
                repositoryPath,
                repositoryName,
                startCommitHash,
                file,
                methodName,
                startLine,
                endLine,
                tracerName);
        model.addAttribute("trace", traceOutput);
        return "method-history";
    }

    @GetMapping("/api/repository-list")
    @ResponseBody
    public List<String> getRepositoryList() {
        return gitRepositoryUiService.findRepositoryList();
    }

    @GetMapping("/api/path-list")
    @ResponseBody
    public List<String> getPathList(
            @RequestParam("repositoryPath") String repositoryPath,
            @RequestParam("repositoryName") String repositoryName,
            @RequestParam("startCommitHash") String startCommitHash,
            @RequestParam("path") String path) {
        return gitRepositoryUiService.findPathList(repositoryPath, repositoryName, startCommitHash, path);
    }

    @GetMapping("/api/method-list")
    @ResponseBody
    public List<MethodLocationDto> getMethodList(
            @RequestParam("repositoryPath") String repositoryPath,
            @RequestParam("repositoryName") String repositoryName,
            @RequestParam("startCommitHash") String startCommitHash,
            @RequestParam("file") String file) {
        return gitRepositoryUiService.findMethodLocationList(repositoryPath, repositoryName, startCommitHash, file);
    }

    @GetMapping("/api/checkout-repository")
    @ResponseBody
    public RepositoryCheckoutResponse checkoutRepository(@RequestParam("location") String location) {
        return gitRepositoryUiService.checkoutRepository(location);
    }
}
