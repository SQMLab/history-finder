/*
I don't need to complete it actually. It's pretty clear that
the algorithm, presented in the paper, does not 100% match with the implementation.
For example, the phase 2 in AbstractParser.java does not match the algorithm..

Interestingly, even in my half implementation, I am some interesting results.
For example, for the checkstyle fireerror method, the original codeshovel misses
efa16e17bf710b7b24af828296c2337612912ba9, although the javadoc comment was changed here.
That means, the original codeshovel did not consider javadoc comments.
 */

package rnd.git.history.finder.algortihm.implementation;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import rnd.git.history.finder.dto.ChangeTag;
import rnd.git.history.finder.dto.FileHistory;
import rnd.git.history.finder.algortihm.Algorithm;
import rnd.git.history.finder.dto.Commit;
import rnd.git.history.finder.dto.Method;
import rnd.git.history.finder.jgit.JgitService;
import rnd.git.history.finder.parser.Parser;
import rnd.git.history.finder.parser.implementation.YJavaParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeShovelAlgorithm implements Algorithm {

    List<FileHistory> fileChangeHistory;
    JaroWinkler jaroWinkler = new JaroWinkler();
    String startFilePath, startCommitName, startMethodCode;
    boolean restart;
    boolean doneWithHistory;

    List<String> modifiedFiles;
    Map<String, RevCommit> allLinkedCommits;
    private YJavaParser javaParser;
    private JgitService jgitService;

    public CodeShovelAlgorithm(Parser parser) {
        this.javaParser = (YJavaParser) parser;
        this.jgitService = this.javaParser.getJgitService();
    }

    @Override
    public void compute(Method method) throws GitAPIException, IOException {

        // step 1, find all change history of the containing file
        startFilePath = method.getMethodHolder().getFile();
        startCommitName = method.getMethodHolder().getCommitHash();
        startMethodCode = method.getMethodHolder().getMethodSourceInfo().getMethodRawSourceCode();
        System.out.println("Started file change tracking");

        fileChangeHistory = listFileChangeHistory(startFilePath, startCommitName);

        System.out.println("Done with file change tracking");
        // allLinkedCommits = JgitService.getAllLinkedCommits(startCommitName);
        doneWithHistory = false;

        while (!doneWithHistory) {
            restart = false;
            boolean found = false;
            for (FileHistory fileHistory : fileChangeHistory) {
                System.out.println("checking in : " + fileHistory.getCommit());
                // if(fileHistory.getCommit().equals("6970485b88600343de72af03a6bf87cf78e6cf55")){
                // System.out.println("found");
                // }
//				Phase 1: Method unchanged
                found = codeShovelPhase1(method, fileHistory);
                if (found) {
                    continue;
                }

//			    Phase 2: Method modified within current file
                found = codeShovelPhase2(method, fileHistory);
                if (found) {
                    if (restart) {
                        break;// go to the while again, because we are tracking a different file now
                    }
                    continue;
                }

//			    Phase 3: Method moved through file rename or move. 
                found = codeShovelPhase3(method, fileHistory);
                if (found) {
                    if (restart) {
                        break;
                    }
                    continue;
                }

//			    Phase 4: Method extracted from different file
                found = codeShovelPhase4(method, fileHistory);
                if (restart) {
                    break;
                }
                if (!found) {
                    doneWithHistory = true;
                    break;
                }
            }

            if (restart) {
                continue;
            }
            if (!doneWithHistory) {
                found = checkForMethodMove(method, startCommitName);
                if (!found) {
                    doneWithHistory = true;
                }
            }
            // doneWithHistory = true;
        }
        addChangeHistory(method, Set.of(ChangeTag.INTRODUCTION));
    }

    // we only need to search where the contained file is modified
    // biggest bottleneck
    private List<FileHistory> listFileChangeHistory(String path, String startCommit)
            throws GitAPIException, IOException {
        return jgitService.listFileChangeHistory(path, startCommit);
    }

    private Boolean isSameMethodSameFile(FileHistory fileHistory) throws IOException {
        return javaParser.IsTheIdenticalMethodHere(startMethodCode, fileHistory.getPath(), fileHistory.getCommit());

    }

    //	Phase 1: Method unchanged
    private boolean codeShovelPhase1(Method method, FileHistory fileHistory) throws IOException {
        boolean found = isSameMethodSameFile(fileHistory);
        if (found) {
            if (!startFilePath.equals(fileHistory.getPath())) {
                //addChangeHistory(method, fileHistory.getCommit(), fileHistory.getPath(), startMethodCode);
                addChangeHistory(method, Set.of(ChangeTag.BODY));
            }
            startFilePath = fileHistory.getPath();
            startCommitName = fileHistory.getCommit();

        }
        return found;
    }

    //  Phase 2: Method modified within current file
    private Boolean codeShovelPhase2(Method method, FileHistory fileHistory) throws IOException {
        String startMethodBody = javaParser.getBodyOfMethod(startMethodCode);
        //body, full method
        Map<String, String> bodyToMethod = javaParser.getAllMethodBodies(fileHistory.getPath(),
                fileHistory.getCommit());
        boolean found = false;
        double maxSim = 0;
        double currentSim;
        String saveMethod = "";
        String saveFile = "";
        String saveCommit = "";
        for (String body : bodyToMethod.keySet()) {
            currentSim = jaroWinkler.similarity(startMethodBody, body);
            if (currentSim > maxSim) {
                maxSim = currentSim;
                saveMethod = bodyToMethod.get(body);
                saveFile = fileHistory.getPath();
                saveCommit = fileHistory.getCommit();
            }
        }
        if (maxSim >= 0.75) {  // body match in current file
            addChangeHistory(method, Set.of(ChangeTag.BODY));
            startMethodCode = saveMethod;
            startFilePath = saveFile;
            startCommitName = saveCommit;
            //addChangeHistory(method, fileHistory.getCommit(), fileHistory.getPath(), startMethodCode);
            found = true;
        }

        return found;
    }

    //  Phase 3: Method moved through file rename or move.
    private Boolean codeShovelPhase3(Method method, FileHistory fileHistory) throws IOException, GitAPIException {

        String startMethodSig = javaParser.getSignatureOfMethod(startMethodCode);
        String startMethodBody = javaParser.getBodyOfMethod(startMethodCode);
        Map<String, String> sigToCode;
        modifiedFiles = jgitService.findModifiedFiles(startCommitName, fileHistory.getCommit());
        boolean found = false;
        for (String modifedFile : modifiedFiles) {
            //signature, full method
            sigToCode = javaParser.getAllMethodSig(modifedFile, fileHistory.getCommit());
            for (String sig : sigToCode.keySet()) {
                if ((jaroWinkler.similarity(sig, startMethodSig) == 1.0) && (jaroWinkler
                        .similarity(javaParser.getBodyOfMethod(sigToCode.get(sig)), startMethodBody) >= 0.5)) {

                    found = true;
                    //addChangeHistory(method, fileHistory.getCommit(), fileHistory.getPath(), sigToCode.get(sig));
                    addChangeHistory(method, Set.of(ChangeTag.BODY));
                    startFilePath = modifedFile;
                    if (!startFilePath.equals(fileHistory.getPath())) {
                        // we need a different file history now
                        fileChangeHistory = jgitService.listFileChangeHistory(startFilePath, fileHistory.getCommit());
                        restart = true;
                    }
                    startCommitName = fileHistory.getCommit();
                    startMethodCode = sigToCode.get(sig);
                    break;
                }

            }
            if (found) {
                break;
            }
        }
        return found;
    }

    //  Phase 4: Method extracted from different file
    private boolean codeShovelPhase4(Method method, FileHistory fileHistory) throws IOException, GitAPIException {

        Boolean found = findInModifiedFiles(method, fileHistory.getCommit());
        return found;
    }

    private Boolean findInModifiedFiles(Method method, String parentCommit) throws IOException, GitAPIException {
        double max = 0;
        double sim;
        String savedMethod = "";
        String savedFile = "";
        boolean found = false;
        String startMethodBody = javaParser.getBodyOfMethod(startMethodCode);

        for (String modifedFile : modifiedFiles) {
            Map<String, String> bodyToMethod = javaParser.getAllMethodBodies(modifedFile, parentCommit);
            for (String body : bodyToMethod.keySet()) {
                sim = jaroWinkler.similarity(startMethodBody, body);
                if (max < sim) {
                    max = sim;
                    savedMethod = bodyToMethod.get(body);
                    savedFile = modifedFile;
                }
            }
        }

        if ((savedMethod.length() < 20) && max >= 0.95) {
            found = true;
        }
        if ((savedMethod.length() >= 20) && max >= 0.82) {
            found = true;
        }
        if (found) {
            //addChangeHistory(method, parentCommit, savedFile, savedMethod);
            addChangeHistory(method, Set.of(ChangeTag.BODY));
            if (!startFilePath.equals(savedFile)) {
                // we need a different file history now
                fileChangeHistory = jgitService.listFileChangeHistory(savedFile, parentCommit);
                restart = true;
            }
            startFilePath = savedFile;
            startCommitName = parentCommit;
            startMethodCode = savedMethod;
        }
        return found;
    }


    private void addChangeHistory(Method method, Set<ChangeTag> changeTagSet) {
        Commit commit = Commit.builder()
                .commitHash(this.startCommitName)
                .methodCode(this.startMethodCode)
                .methodCode(null)
                .methodContainerFile(this.startFilePath)
                .commitInfo(jgitService.buildCommitInfo(this.startCommitName))
                .build();
        method.addCommitInHistory(commit, changeTagSet);
    }

    private boolean checkForMethodMove(Method method, String startCommitName) throws IOException, GitAPIException {
        RevCommit revCommit = jgitService.getRevCommit(startCommitName);
        String parentCommitName = revCommit.getParent(0).getName();
        modifiedFiles = jgitService.findModifiedFiles(startCommitName, parentCommitName);
        return findInModifiedFiles(method, parentCommitName);
    }
}