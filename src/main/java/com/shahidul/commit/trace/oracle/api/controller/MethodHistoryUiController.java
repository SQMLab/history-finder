package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.api.payload.RepositoryListResponse;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.ui.GitRepositoryUiService;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@AllArgsConstructor
@Slf4j
public class MethodHistoryUiController {
    GitRepositoryUiService gitRepositoryUiService;

    @GetMapping("ui/method-selector")
    public String showMethodSelectorUi() {
        return "method-selector";
    }

    @GetMapping({"ui/method-history", "/"})
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
        try {

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
        } catch (Exception e) {
            log.error("Failed to execute trace", e);
            throw new CtoException(CtoError.Failed_To_Execute_Trace, e);
        }
        return "method-history";
    }

    @GetMapping("/api/repository-list")
    @ResponseBody
    public RepositoryListResponse getRepositoryList() {
        try {
            return gitRepositoryUiService.findRepositoryList();
        } catch (Exception e) {
            log.error("Failed to find repositories", e);
            throw new CtoException(CtoError.Failed_To_Find_Repositories, e);
        }
    }

    @GetMapping("/api/path-list")
    @ResponseBody
    public List<String> getPathList(
            @RequestParam("repositoryPath") String repositoryPath,
            @RequestParam("repositoryName") String repositoryName,
            @RequestParam("startCommitHash") String startCommitHash,
            @RequestParam("path") String path) {
        try {
            return gitRepositoryUiService.findPathList(repositoryPath, repositoryName, startCommitHash, path);
        } catch (Exception e) {
            log.error("Failed to find paths", e);
            throw new CtoException(CtoError.Failed_To_Find_Paths, e);
        }
    }

    @GetMapping("/api/method-list")
    @ResponseBody
    public List<MethodLocationDto> getMethodList(
            @RequestParam("repositoryPath") String repositoryPath,
            @RequestParam("repositoryName") String repositoryName,
            @RequestParam("startCommitHash") String startCommitHash,
            @RequestParam("file") String file) {
        try {
            return gitRepositoryUiService.findMethodLocationList(repositoryPath, repositoryName, startCommitHash, file);
        } catch (Exception e) {
            log.error("Failed to find methods", e);
            throw new CtoException(CtoError.Failed_To_Find_Methods, e);
        }
    }

    @GetMapping("/api/checkout-repository")
    @ResponseBody
    public RepositoryCheckoutResponse checkoutRepository(@RequestParam("location") String location) {
        try {
            return gitRepositoryUiService.checkoutRepository(location);
        } catch (Exception e) {
            log.error("Failed to checkout repository", e);
            throw new CtoException(CtoError.Git_Checkout_Failed, e);
        }
    }
}
