package com.shahidul.commit.trace.oracle.api.controller.ui;

import com.shahidul.commit.trace.oracle.core.ui.GitRepositoryUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("ui/method-history")
    public String showMethodHistoryUi(Model model) {
        // Populate results for demonstration
        model.addAttribute("results", null);
        return "method-history";
    }
    @GetMapping("/api/repository-list")
    @ResponseBody
    public List<String> getRepositoryList() {
        // Sample list, in a real scenario, this would query a database or file system
        return gitRepositoryUiService.findRepositoryList();
    }
    @GetMapping("/api/path-list")
    @ResponseBody
    public List<String> getPathList(@RequestParam("repositoryName") String repositoryName,
                                    @RequestParam("startCommitHash") String startCommitHash,
                                    @RequestParam("path") String path) {
        return gitRepositoryUiService.findPathList(repositoryName, startCommitHash, path);
    }
}
