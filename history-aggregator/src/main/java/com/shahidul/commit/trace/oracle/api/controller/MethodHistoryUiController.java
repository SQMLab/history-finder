package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.api.payload.RepositoryListResponse;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.error.CtoError;
import com.shahidul.commit.trace.oracle.core.error.exception.BaseException;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.model.HistoryInputParam;
import com.shahidul.commit.trace.oracle.core.ui.GitRepositoryUiService;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Controller
@AllArgsConstructor
@Slf4j
public class MethodHistoryUiController {
    GitRepositoryUiService gitRepositoryUiService;

    @GetMapping({"/ui/method-history", "/"})
    public String showMethodSelectorUi(
            @RequestParam(required = false) String tracerName,
            @RequestParam(required = false) String startCommitHash,
            @RequestParam(required = false) String repositoryHostName,
            @RequestParam(required = false) String repositoryAccountName,
            @RequestParam(required = false) String repositoryName,
            @RequestParam(required = false) String repositoryPath,
            @RequestParam(required = false) String repositoryLocation,
            @RequestParam(required = false) String file,
            @RequestParam(required = false) String methodName,
            @RequestParam(required = false) String startLine,
            @RequestParam(required = false) String endLine,
            @RequestParam(required = false) String oracleFileId,
            Model model) {

        model.addAttribute("tracerName", tracerName);
        model.addAttribute("startCommitHash", startCommitHash);
        model.addAttribute("repositoryHostName", repositoryHostName);
        model.addAttribute("repositoryAccountName", repositoryAccountName);
        model.addAttribute("repositoryName", repositoryName);
        model.addAttribute("repositoryPath", repositoryPath);
        model.addAttribute("repositoryLocation", repositoryLocation);
        model.addAttribute("file", file);
        model.addAttribute("methodName", methodName);
        model.addAttribute("startLine", startLine);
        model.addAttribute("endLine", endLine);
        model.addAttribute("oracleFileId", oracleFileId);

        return "method-history";
    }

    @GetMapping({"api/method-history"})
    @ResponseBody
    public CommitTraceOutput getMethodHistoryUi(@RequestParam("repositoryHostName") String repositoryHostName,
                                                @RequestParam("repositoryAccountName") String repositoryAccountName,
                                                @RequestParam("repositoryPath") String repositoryPath,
                                                @RequestParam("repositoryName") String repositoryName,
                                                @RequestParam("startCommitHash") String startCommitHash,
                                                @RequestParam("file") String file,
                                                @RequestParam("methodName") String methodName,
                                                @RequestParam("startLine") Integer startLine,
                                                @RequestParam("endLine") Integer endLine,
                                                @RequestParam("tracerName") TracerName tracerName,
                                                @RequestParam(value = "forceExecute", required = false) Boolean forceExecute
    ) {
        CommitTraceOutput traceOutput = gitRepositoryUiService.findMethodHistory(repositoryHostName,
                repositoryAccountName,
                repositoryPath,
                repositoryName,
                startCommitHash,
                file,
                methodName,
                startLine,
                endLine,
                tracerName,
                forceExecute != null ? forceExecute : false
        );

        if (traceOutput.getCommitDetails().isEmpty()) {
            if (TracerName.GIT_FUNC_NAME.equals(tracerName)) {
                throw new CtoException(CtoError.Git_Function_Name_Failure);
            } else {
                String formattedName = Character.toUpperCase(tracerName.getCode().charAt(0)) + tracerName.getCode().substring(1);
                throw new BaseException(CtoError.Tool_X_Failed_To_Generate_Method_History.getCode(), CtoError.Tool_X_Failed_To_Generate_Method_History.getMsg().formatted(formattedName));
            }
        }
        return traceOutput;

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
            String formattedPath = path.replaceAll("[/\\\\]+$", "");
            return gitRepositoryUiService.findPathList(repositoryPath, repositoryName, startCommitHash, formattedPath);
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
            if (e instanceof BaseException) {
                throw e;
            } else {
                throw new CtoException(CtoError.Git_Checkout_Failed, e);
            }
        }
    }

    @GetMapping({"ui/oracle-list"})
    public String oracleList(Model model) {

        return "oracle-list";
    }

    @GetMapping("/api/oracle-files")
    @ResponseBody
    public List<String> getOracleFiles() {
        return gitRepositoryUiService.getOracleFileList();

    }

    @GetMapping({"ui/view-oracle-method-history"})
    public String viewOracleMethodHistory(
            @RequestParam("oracleFile") String oracleFile,
            @RequestParam("tracerName") TracerName tracerName,
            Model model) {
        try {
            HistoryInputParam historyInputParam = gitRepositoryUiService.findOracleMethodHistory(oracleFile, tracerName);
            return "redirect:/ui/method-history" +
                    "?tracerName=" + tracerName +
                    "&startCommitHash=" + historyInputParam.getStartCommitHash() +
                    "&repositoryHostName=" + URLEncoder.encode(historyInputParam.getRepositoryHostName(), StandardCharsets.UTF_8) +
                    "&repositoryAccountName=" + historyInputParam.getRepositoryAccountName() +
                    "&repositoryName=" + historyInputParam.getRepositoryName() +
                    "&repositoryPath=" + URLEncoder.encode(historyInputParam.getRepositoryPath(), StandardCharsets.UTF_8) +
                    "&repositoryLocation=" + URLEncoder.encode(historyInputParam.getRepositoryUrl(), StandardCharsets.UTF_8) +
                    "&file=" + URLEncoder.encode(historyInputParam.getFile(), StandardCharsets.UTF_8) +
                    "&methodName=" + historyInputParam.getMethodName() +
                    "&startLine=" + historyInputParam.getStartLine() +
                    "&endLine=" + historyInputParam.getEndLine();
        } catch (Exception e) {
            log.error("Failed to execute trace", e);
            throw new CtoException(CtoError.Failed_To_Execute_Trace, e);
        }
    }
}
