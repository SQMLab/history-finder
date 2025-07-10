package rnd.git.history.finder.util;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import rnd.git.history.finder.Util;
import rnd.git.history.finder.dto.*;
import rnd.git.history.finder.jgit.JgitService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 2025-07-10
 */
public class HistoryFinderOutputConverterImpl implements HistoryFinderOutputConverter {
    @Override
    public CommitTraceOutput convert(JgitService jgitService, HistoryFinderInput historyFinderInput, List<HistoryEntry> historyEntryList, Long executionTime, Integer analyzedCommitCount, String methodId) {
        List<InputCommit> commitList = historyEntryList.stream().map(historyEntry -> InputCommit.builder()
                .commitHash(historyEntry.getNewMethodHolder().getCommitHash())
                .changeTags(historyEntry.getChangeTagSet().stream().toList())
                .build()).toList();
        List<OutputCommitDetail> commitDetailList = toCommitDetailList(jgitService, historyEntryList);
        String tracerName = "historyFinder";
        return CommitTraceOutput.builder()
                .tracerName(tracerName)
                .displayTracerName("History Finder")
                .repositoryName(historyFinderInput.getRepositoryName())
                .repositoryUrl(jgitService.getRepositoryUrl())
                .startCommitHash(historyFinderInput.getStartCommitHash())
                .file(historyFinderInput.getFile())
                .fileName(Util.extractLastPart(historyFinderInput.getFile()))
                .language(historyFinderInput.getLanguageType())
                .elementType("method")
                .element(historyFinderInput.getMethodName())
                .startLine(historyFinderInput.getStartLine())
                .endLine(null)
                .runtime(executionTime)
                .methodId(methodId)
                .analyzedCommitCount(analyzedCommitCount)
                .precision(null)
                .recall(null)
                .commits(commitList)
                .commitMap(commitList.stream().collect(Collectors.toMap(InputCommit::getCommitHash,
                        commit -> ChangeTagUtil.toCodeShovelChangeText(commit.getChangeTags().stream().toList()),
                        (x, y) -> x, LinkedHashMap::new)))
                .commitHashes(historyEntryList.stream().map(entry -> entry.getNewMethodHolder().getCommitHash()).toList())
                .commitDetails(commitDetailList)
                .commitDetailMap(commitDetailList.stream().collect(Collectors.toMap(OutputCommitDetail::getCommitHash,
                        commit -> commit,
                        (x, y) -> x,
                        LinkedHashMap::new)))
                .build();
    }

    private List<OutputCommitDetail> toCommitDetailList(JgitService jgitService, List<HistoryEntry> historyEntryList) {
        return historyEntryList.stream()
                .map(historyEntry -> {
                    OutputCommitDetail commitDetail = toOutputCommitDetailWithoutSubChange(jgitService, historyEntry);
                    if (historyEntry.getChangeTagSet().size() > 1) {
                        List<OutputCommitDetail> subChangeCommitList = historyEntry.getChangeTagSet().stream().map(changeTag -> {
                            OutputCommitDetail subCommitDetail = toOutputCommitDetailWithoutSubChange(jgitService, historyEntry);
                            List<ChangeTag> subChangeTagList = changeTag != null ? List.of(changeTag) : Collections.emptyList();
                            subCommitDetail.setChangeTags(subChangeTagList);
                            subCommitDetail.setDisplayChangeTags(displayChangeTags(subChangeTagList));
                            subCommitDetail.setChangeTagText(ChangeTagUtil.toCodeShovelChangeText(subChangeTagList));
                            subCommitDetail.setAdditionalCommitInfo(toAdditionalCommitInfo(historyEntry.getOldMethodHolder(), historyEntry.getNewMethodHolder()));
                            return subCommitDetail;
                        }).toList();
                        commitDetail.setSubChangeList(subChangeCommitList);
                    }
                    return commitDetail;
                })
                .toList();
    }

    private OutputCommitDetail toOutputCommitDetailWithoutSubChange(JgitService jgitService, HistoryEntry historyEntry) {
        MethodHolder newMethodHolder = historyEntry.getNewMethodHolder();
        MethodHolder oldMethodHolder = historyEntry.getOldMethodHolder();
        List<ChangeTag> changeTagList = historyEntry.getChangeTagSet().stream().toList();
        RevCommit revCommit = jgitService.getRevCommit(newMethodHolder.getCommitHash());
        RevCommit revAncestorCommit = historyEntry.getAncestorCommitHash() != null ? jgitService.getRevCommit(historyEntry.getAncestorCommitHash()) : null;

        PersonIdent authorIdent = revCommit.getAuthorIdent();

        String repositoryUrl = jgitService.getRepositoryUrl();
        OutputCommitDetail.OutputCommitDetailBuilder commitDetailBuilder = OutputCommitDetail.builder();
        commitDetailBuilder
                .commitHash(newMethodHolder.getCommitHash())
                .committedAt(new Date(revCommit.getCommitTime() * 1000L))
                .startLine(newMethodHolder.getMethodSourceInfo().getStartLine())
                .endLine(newMethodHolder.getMethodSourceInfo().getEndLine())
                .newFile(newMethodHolder.getFile())
                .oldFile(oldMethodHolder != null ? oldMethodHolder.getFile() : null)
                .changeTags(changeTagList)
                .displayChangeTags(displayChangeTags(changeTagList))
                .changeTagText(ChangeTagUtil.toCodeShovelChangeText(changeTagList))
                .author(authorIdent != null ? authorIdent.getName() : null)
                .email(authorIdent != null ? authorIdent.getEmailAddress() : null)
                .shortMessage(revCommit.getShortMessage())
                .fullMessage(revCommit.getFullMessage())
                .daysBetweenCommits(Util.daysBetweenCommit(revCommit.getCommitTime(), revAncestorCommit != null ? revAncestorCommit.getCommitTime() : revCommit.getCommitTime()))
                .diff(Util.getDiff(oldMethodHolder != null ? oldMethodHolder.getMethodSourceInfo().getFullCode() : null, newMethodHolder.getMethodSourceInfo().getFullCode()))
                .docDiff(Util.getDiff(oldMethodHolder != null ? rnd.git.history.finder.Util.extractJavaDoc(oldMethodHolder.getMethodSourceInfo().getMethodDeclaration()) : null,
                        rnd.git.history.finder.Util.extractJavaDoc(newMethodHolder.getMethodSourceInfo().getMethodDeclaration())))
                .commitUrl(Util.getCommitUrl(repositoryUrl, newMethodHolder.getCommitHash()))
                .diffUrl(Util.getDiffUrl(repositoryUrl, oldMethodHolder != null ? oldMethodHolder.getCommitHash() : null, newMethodHolder.getCommitHash(), newMethodHolder.getFile()))
                .authorSearchUrl(Util.getUserSearchUrl(authorIdent != null ? authorIdent.getName() : null))
                .oldFileUrl(Util.gitRawFileUrl(repositoryUrl, oldMethodHolder != null ? oldMethodHolder.getCommitHash() : null, oldMethodHolder != null ? oldMethodHolder.getFile() : null, newMethodHolder.getMethodSourceInfo().getStartLine()))
                .newFileUrl(Util.gitRawFileUrl(repositoryUrl, newMethodHolder.getCommitHash(), newMethodHolder.getFile(), newMethodHolder.getMethodSourceInfo().getStartLine()))
                .additionalCommitInfo(toAdditionalCommitInfo(oldMethodHolder, newMethodHolder))
                .newCode(newMethodHolder.getMethodSourceInfo().getMethodRawSourceCode())
                .diffDetail(null);
        if (revAncestorCommit != null) {
            commitDetailBuilder.commitCountBetweenForRepo(jgitService.countCommit(revCommit, revAncestorCommit, null))
                    .commitCountBetweenForFile(jgitService.countCommit(revCommit, revAncestorCommit, newMethodHolder.getFile()));
        }
        return commitDetailBuilder.build();


    }

    private AdditionalCommitInfo toAdditionalCommitInfo(MethodHolder oldMethodHolder, MethodHolder newMethodHolder) {
        AdditionalCommitInfo.AdditionalCommitInfoBuilder additionalCommitInfoBuilder = AdditionalCommitInfo.builder()
                .newMethodName(newMethodHolder.getMethodSourceInfo().getMethodDeclaration().getName().asString())
                .newSignature(newMethodHolder.getMethodSourceInfo().getMethodDeclaration().getSignature().asString())
                .newFile(newMethodHolder.getFile());
        if (oldMethodHolder != null) {
            additionalCommitInfoBuilder
                    .oldMethodName(oldMethodHolder.getMethodSourceInfo().getMethodDeclaration().getName().asString())
                    .oldSignature(oldMethodHolder.getMethodSourceInfo().getMethodDeclaration().getSignature().asString())
                    .oldFile(oldMethodHolder.getFile());
        }
        return additionalCommitInfoBuilder.build();
    }

    private List<String> displayChangeTags(List<ChangeTag> changeTags) {
        if (changeTags == null) {
            return Collections.emptyList();
        }
        return changeTags.stream()
                .map(tag -> displayText(tag.getCode()))
                .toList();

    }

    private static String displayText(String tag) {
        return Arrays.stream(tag.split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }
}
