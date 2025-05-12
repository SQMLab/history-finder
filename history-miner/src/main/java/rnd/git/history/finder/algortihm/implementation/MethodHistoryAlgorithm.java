package rnd.git.history.finder.algortihm.implementation;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.revwalk.RevCommit;
import rnd.git.history.finder.Util;
import rnd.git.history.finder.dto.*;
import rnd.git.history.finder.MethodComparator;
import rnd.git.history.finder.algortihm.Algorithm;
import rnd.git.history.finder.jgit.JgitService;
import rnd.git.history.finder.jgit.JgitService.FileKey;
import rnd.git.history.finder.parser.Parser;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;
import rnd.git.history.finder.parser.implementation.YJavaParser;

@Slf4j
public class MethodHistoryAlgorithm implements Algorithm {
    private static final boolean USE_SIM_HASH_BASED_MATCHING = false;
    private static final long SEARCH_TIME_OUT_IN_SECONDS = 600;


    private YJavaParser javaParser;
    private JgitService jgitService;

    public MethodHistoryAlgorithm(Parser javaParser) {
        this.javaParser = (YJavaParser) javaParser;
        this.jgitService = this.javaParser.getJgitService();
    }

    @Override
    public void compute(Method candidateMethod) {
        Instant startTime = Instant.now();
        Queue<MethodHolder> stageQueue = new ArrayDeque<>();
        stageQueue.add(candidateMethod.getMethodHolder());
        /*try {
            stageQueue.add((MethodHolder) candidateMethod.getMethodHolder().clone());
        } catch (CloneNotSupportedException e) {
            log.error("Cannot clone MethodSourceInfo", e);
            throw new RuntimeException(e);
        }*/
        log.info("Tracing started with start commit {}", candidateMethod.getMethodHolder().getCommitHash());
//***		Collect the change commit history C of F without renaming
//***		C = getFileChangeHistory(F)
        int analyzedCommitCount = 0;
        NodeContext furthestNode = null;
        int furthestNodeCommitTime = (int) (System.currentTimeMillis()/ 1000);
        int searchCycleCount = 0;
        while (!stageQueue.isEmpty()) {
            searchCycleCount += 1;
            MethodHolder stageStartMethodHolder = stageQueue.poll();
            log.info("******** Cycling {} *******", searchCycleCount);
            log.info("Cycle start commit hash {}", stageStartMethodHolder.getCommitHash());
            log.info("Cycle start File {}", stageStartMethodHolder.getFile());

            Graph graph = jgitService.gitLogFileAsGraph(stageStartMethodHolder.getFile(), stageStartMethodHolder.getCommitHash());
            Queue<NodeContext> bfsQueue = new ArrayDeque<>();
            NodeContext startNodeContext = NodeContext.builder()
                    .commitHash(graph.getSourceNode())
                    .file(stageStartMethodHolder.getFile())
                    .methodName(stageStartMethodHolder.getMethodSourceInfo().getMethodDeclaration().getNameAsString())
                    .fullMethodSignature(stageStartMethodHolder.getMethodSourceInfo().getMethodDeclaration().getDeclarationAsString())
                    .startLine(stageStartMethodHolder.getMethodSourceInfo().getStartLine())
                    .build();
            bfsQueue.add(startNodeContext);
            Set<String> visitedNode = new LinkedHashSet<>();
            visitedNode.add(startNodeContext.getCommitHash());


            //*** Step 1
            /**
             * for each commit Ci in C, find out whether method change or not
             */
            while (!bfsQueue.isEmpty()) {
                analyzedCommitCount += 1;
                NodeContext contextU = bfsQueue.poll();
                MethodHolder methodU = null;
                if (contextU.getCommitHash().startsWith("5bd6")) {
                    log.info("Found target commit{}", contextU.getCommitHash());
                }
                try {
                    methodU = javaParser.findMethod(contextU.getCommitHash(), contextU.getFile(), contextU.getMethodName(), contextU.getFullMethodSignature());
                } catch (Exception e) {
                    log.error("Method not found", e);
                    continue;
                }

                /*if (contextU.getName().equals(methodU.getCommitHash())) //Start commit
                    continue;*/

                log.info("=========== Iterating {}/{}==============", searchCycleCount, contextU.getCommitHash());
                log.info("Commit {} ", contextU.getCommitHash());
                if (methodU == null) {
                    log.info("Why child is null?");
                    throw new RuntimeException("Method U not found??");
                }
                String file = methodU.getFile();
                log.info("File {}", file);
                log.info("Method Signature : {}", methodU.getMethodSourceInfo().getMethodDeclaration().getSignature());
                log.info("Method Declaration : {}", methodU.getMethodSourceInfo().getMethodDeclaration().getDeclarationAsString());

                RevCommit revCommitU = jgitService.getRevCommit(contextU.getCommitHash());
                if(revCommitU.getCommitTime() <= furthestNodeCommitTime){
                    furthestNode = contextU;
                    furthestNodeCommitTime = revCommitU.getCommitTime();
                }
                int changeIndex = 0;
                Map<String, MethodMap> methodMapCache = new HashMap<>();
                for (RevCommit directParentRevCommit : revCommitU.getParents()) {
                    try {
                        methodMapCache.put(directParentRevCommit.getName(), javaParser.getAllMethodsInFile(directParentRevCommit.getName(), file));
                    } catch (Exception e) {
                        log.debug("Failed to extract all methods in file {}.", file, e);
                    }
                }
                List<String> ancestorCommitList = new ArrayList<>(graph.getParentList(contextU.getCommitHash()));
                while (changeIndex < 3) {
                    List<HistoryEntry> fringeHistoryEntryList = new ArrayList<>();
                    RevCommit[] parents = revCommitU.getParents();
                    for (int parentIndex = 0; parentIndex < parents.length; parentIndex++) {
                        RevCommit directParentRevCommit = parents[parentIndex];
//*** collect all methods Mi in F.
                        String directParentCommitHash = directParentRevCommit.getName();
                        MethodMap directParentFileMethodMap = methodMapCache.getOrDefault(directParentCommitHash, null);
                        MethodHolder directParentMethodHolder = null;
                        try {
                            directParentMethodHolder = switch (changeIndex) {
                                case 0 ->
                                        directParentFileMethodMap != null ? searchInsideFileWithSignatureMatching(methodU, directParentFileMethodMap, directParentCommitHash, file) : null;
                                case 1 ->
                                        directParentFileMethodMap != null ? searchInsideFileWithBodySimilarityMatching(directParentFileMethodMap, methodU, directParentCommitHash, file, false) : null;
                                case 2 -> searchInParentCommit(methodU);
                                default -> throw new RuntimeException("Not implemented yet");
                            };
                        } catch (Exception e) {
                            log.error("Failed to extract all methods in file {}.", file, e);
                        }


                      /*  boolean isFoundInOtherFile = false;
                        if (directParentMethodHolder == null) {
                            directParentMethodHolder = searchInParentCommit(methodU);
                            isFoundInOtherFile = true;
                        }*/
                        if (directParentMethodHolder != null) {
                            int ancestorIndex = Math.min(parentIndex, ancestorCommitList.size() - 1);
                            Set<ChangeTag> changeTags = detectChangeList(methodU, directParentMethodHolder);
                            HistoryEntry historyEntry = HistoryEntry.builder()
                                    .oldMethodHolder(directParentMethodHolder)
                                    .newMethodHolder(methodU)
                                    .ancestorCommitHash(ancestorIndex >= 0 && ancestorIndex < ancestorCommitList.size() ? ancestorCommitList.get(ancestorIndex) : null)
                                    .changeTagSet(changeTags)
                                    .build();
                            fringeHistoryEntryList.add(historyEntry);
                        }
                    }
                    boolean isFoundInOtherFile = (changeIndex == 2);
                    if (fringeHistoryEntryList.size() > 1 && isFoundInOtherFile){
                     /*   fringeHistoryEntryList.sort((h1, h2)-> {
                            int h1Index = h1.getChangeTagSet().contains(ChangeTag.MOVE) || h1.getChangeTagSet().contains(ChangeTag.FILE_MOVE) ? 0 : -1;
                            int h2Index = h2.getChangeTagSet().contains(ChangeTag.MOVE) || h2.getChangeTagSet().contains(ChangeTag.FILE_MOVE)? 0 : -1;
                            return h1Index - h2Index;
                        });*/
                        //TODO : pick the best matching one
                        while (fringeHistoryEntryList.size() > 1){
                            fringeHistoryEntryList.removeLast();
                        }
                    }
                    boolean isThereExactMatching = fringeHistoryEntryList.stream()
                            .anyMatch(historyEntry -> historyEntry.getChangeTagSet().isEmpty());

                    for (HistoryEntry historyEntry : fringeHistoryEntryList) {
                        if (!historyEntry.getChangeTagSet().isEmpty() && !isThereExactMatching) {
                            candidateMethod.addHistoryEntry(historyEntry);
                        }
                        if (isFoundInOtherFile) {
                            stageQueue.add(historyEntry.getOldMethodHolder());
                        }
                    }
                    if (!isFoundInOtherFile && !fringeHistoryEntryList.isEmpty()) {
                        int ancestorIndex = 0;
                        for (String v : ancestorCommitList) {
                            if (!visitedNode.contains(v)) {
                                int targetAncestorIndex = ancestorIndex < fringeHistoryEntryList.size() ? ancestorIndex : 0;
                                MethodHolder oldMethodHolder = fringeHistoryEntryList.get(targetAncestorIndex).getOldMethodHolder();
                                NodeContext nodeContextV = NodeContext.builder()
                                        .commitHash(v)
                                        .file(oldMethodHolder.getFile())
                                        .methodName(oldMethodHolder.getMethodSourceInfo().getMethodDeclaration().getNameAsString())
                                        .fullMethodSignature(oldMethodHolder.getMethodSourceInfo().getMethodDeclaration().getDeclarationAsString())
                                        .startLine(oldMethodHolder.getMethodSourceInfo().getStartLine())
                                        .build();
                                bfsQueue.add(nodeContextV);
                                visitedNode.add(v);
                            }
                            ancestorIndex += 1;
                        }
                    }
                    changeIndex += 1;
                    if (!fringeHistoryEntryList.isEmpty()){
                        break;
                    }
                }


            }


            log.info("Search cycle count {}", searchCycleCount);
            log.info("Search time elapsed {}", Duration.between(startTime, Instant.now()));

            //addChangeHistory(candidateMethod, stageStartMethodHolder, null, Set.of(ChangeTag.INTRODUCTION));
        }
        MethodHolder introductionMethodHolder;
        introductionMethodHolder = javaParser.findMethod(furthestNode.getCommitHash(), furthestNode.getFile(), furthestNode.getMethodName(), furthestNode.getStartLine());
        candidateMethod.getHistoryEntryList().add(HistoryEntry.builder()
                .newMethodHolder(introductionMethodHolder)
                .oldMethodHolder(null)
                .changeTagSet(Set.of(ChangeTag.INTRODUCTION))
                .build());
        candidateMethod.setAnalyzedCommitCount(analyzedCommitCount);
    }

    /*  ***	If found == false:
     ***   ## method signature is changed (w/wo body and other parts)
     ***	or method was moved,
     ***	or method was introduced,33c19b8b14b534ba423c9cee51c90536daa6e119
     ***	what to do?? Let’s follow codeshovel for this part…
     */
    private MethodHolder searchInsideFileWithSignatureMatching(MethodHolder childMethodContext, MethodMap parentFileMethodMap, String commitHash, String file) {

        MethodSourceInfo matchingCandidate = parentFileMethodMap
                .getBySignature(childMethodContext.getMethodSourceInfo().getMethodDeclaration());
//*** for all methids Mi in F
        // check method content similarity
        if (matchingCandidate != null) {
            // look for full method exact match in the same file
            // first match with AST version, which ignores formatting change
            return MethodHolder.builder()
                    .commitHash(commitHash)
                    .file(file)
                    .methodSourceInfo(matchingCandidate)
                    .build();
        } else {
            return null;
        }
    }


    // cann't find a match candidate in current file, need to check other files
    // modified in this commit
    private MethodHolder searchInsideFileWithBodySimilarityMatching(MethodMap parentFileMethodMap, MethodHolder childMethodHolder, String commitHash, String file, boolean isOtherFile) {
        //##check the same file
        // ## method was changed or introduced
        // match by body as we already done matching sigs on these methods
        log.info("=============== searchInsideFileWithBodySimilarityMatching ==================");
        MethodMap childFileMethodMapping = javaParser.getAllMethodsInFile(childMethodHolder.getCommitHash(), childMethodHolder.getFile());
        MethodMap methodMapDiff = subtract(parentFileMethodMap, childFileMethodMapping);

        List<MethodSourceInfo> nameMatchingMethodList = !isOtherFile ? methodMapDiff.getAllByMethodName(childMethodHolder.getMethodSourceInfo().getMethodDeclaration().getNameAsString()) : new ArrayList<>();
        MethodMap methodMapToSearch = null;
        if (!nameMatchingMethodList.isEmpty()) {
            methodMapToSearch = new MethodMap();
            for (MethodSourceInfo methodSourceInfo : nameMatchingMethodList) {
                methodMapToSearch.put(methodSourceInfo);
            }
        } else {
            methodMapToSearch = methodMapDiff;
        }

        List<MethodSimilarity> methodSimilarityList;
        if (USE_SIM_HASH_BASED_MATCHING) {
            methodSimilarityList = MethodComparator
                    .findBestMatchingCandidateByMethodBody2(methodMapToSearch, childMethodHolder.getMethodSourceInfo());
        } else {
            methodSimilarityList = MethodComparator.findBestMatchingWithJaroWinkler(methodMapToSearch, childMethodHolder.getMethodSourceInfo());
        }


        Optional<MethodSimilarity> optionalTopMethodSimilarity = MethodComparator.findTop(methodSimilarityList);
        // match by body as we already done matching sigs on these methods
        if (optionalTopMethodSimilarity.isPresent()) {// ## method moved or introduced
            List<String> lineList = Util.toLineList(childMethodHolder.getMethodSourceInfo().getMethodRawSourceCode());
            double overallTargetSimilarity;
            if (lineList.size() >= 5) {
                overallTargetSimilarity = 0.70;
            } else {
                overallTargetSimilarity = 0.75;
            }
            MethodSimilarity methodSimilarity = optionalTopMethodSimilarity.get();
            if (methodSimilarity.getOverallSimilarity() >= overallTargetSimilarity || !nameMatchingMethodList.isEmpty()) {
                return MethodHolder.builder().commitHash(commitHash).file(file)
                        .methodSourceInfo(methodSimilarity.getMethodSourceInfo())
                        .build();
            }
        }
        return null;
    }


    private MethodHolder searchOtherModifiedFiles(MethodHolder childMethodHolder, String likelyParentCommit) {
        //***					 List_files = modified_files(ci)
        log.info("============ searchOtherModifiedFiles ===================");
        List<FileChangeDto> fileChangeList = jgitService.findModifiedFilesWithModType(childMethodHolder.getCommitHash(), likelyParentCommit, childMethodHolder.getFile());

        Set<FileChangeDto> matchingRenamedFileList = fileChangeList.stream()
                .filter(fileChange -> fileChange.getChangeType() == ChangeType.RENAME && fileChange.getNewFile().equalsIgnoreCase(childMethodHolder.getFile()))
                .collect(Collectors.toCollection(HashSet::new));

        if (!matchingRenamedFileList.isEmpty()) {
            for (FileChangeDto fileChangeDto : matchingRenamedFileList) {
                String oldFile = fileChangeDto.getOldFile();
                MethodMap parentMethodMap = javaParser.getAllMethodsInFile(likelyParentCommit, oldFile);
                MethodHolder parentMethodHolder = searchInsideFileWithSignatureMatching(childMethodHolder, parentMethodMap, likelyParentCommit, oldFile);
                if (parentMethodHolder == null) {
                    parentMethodHolder = searchInsideFileWithBodySimilarityMatching(parentMethodMap, childMethodHolder, likelyParentCommit, oldFile, true);
                }
                if (parentMethodHolder != null) {
                    parentMethodHolder.setFileChangeDto(fileChangeDto);
                    return parentMethodHolder;
                }

            }
        }
        List<FileChangeDto> changeListWithoutRenamedOriginalFile = fileChangeList.stream()
                .filter(fileChangeDto -> !matchingRenamedFileList.contains(fileChangeDto))
                .collect(Collectors.toCollection(ArrayList::new));

        List<List<FileChangeDto>> searchGroupList = groupBySearchCategory(changeListWithoutRenamedOriginalFile, childMethodHolder.getFile());
        for (List<FileChangeDto> searchGroup : searchGroupList) {
            MethodHolder matchingCandidateMethodHolder = findBestMatchingCandidateInCommitModifiedFile(searchGroup, likelyParentCommit, childMethodHolder);
            if (matchingCandidateMethodHolder != null) {
                return matchingCandidateMethodHolder;
            }
        }
        return null;
    }


    private MethodHolder searchInParentCommit(MethodHolder childMethodHolder) {
        log.info("============= searchInParentCommit() ==================");

        for (RevCommit parentRevCommit : jgitService.getRevCommit(childMethodHolder.getCommitHash()).getParents()) {
            MethodHolder parentMethodHolder = searchOtherModifiedFiles(childMethodHolder, parentRevCommit.getName());
            if (parentMethodHolder != null) {
                return parentMethodHolder;
            }
        }
        return null;
    }

    private List<List<FileChangeDto>> groupBySearchCategory(List<FileChangeDto> fileChangeList, String file) {

        List<FileChangeDto> filteredChangeOpsList = fileChangeList.stream()
                .filter(changeOpsEntry -> Set.of(ChangeType.MODIFY,/* ChangeType.ADD, ChangeType.COPY,*/ ChangeType.DELETE, ChangeType.RENAME).contains(changeOpsEntry.getChangeType())).collect(Collectors.toCollection(ArrayList::new));

        String clazzFileNameWithExtension = Util.extractFileName(file);
        List<FileChangeDto> classNameMatchingGroup = filteredChangeOpsList.stream().filter(entry -> entry.getOldFile().contains(clazzFileNameWithExtension))
                .collect(Collectors.toCollection(ArrayList::new));

        List<FileChangeDto> fileOpsGroup = filteredChangeOpsList.stream().filter(entry -> entry.getChangeType() != ChangeType.MODIFY).collect(Collectors.toCollection(ArrayList::new));
        List<FileChangeDto> modifiedGroup = filteredChangeOpsList.stream().filter(entry -> entry.getChangeType() == ChangeType.MODIFY).collect(Collectors.toCollection(ArrayList::new));


        List<List<FileChangeDto>> searchGroupList = Stream.of(classNameMatchingGroup, fileOpsGroup, modifiedGroup).filter(group -> !group.isEmpty()).toList();

        //TODO : Removing sorting
        for (List<FileChangeDto> searchGroup : searchGroupList) {
            sort(searchGroup, clazzFileNameWithExtension, List.of(ChangeType.RENAME, ChangeType.DELETE, ChangeType.MODIFY));
        }
        return searchGroupList;
    }


    private void sort(List<FileChangeDto> entryList, String classFileName, List<ChangeType> sortOrder) {
        entryList.sort((entryX, entryY) -> {
            int xIndex = sortOrder.indexOf(entryX.getChangeType());
            int yIndex = sortOrder.indexOf(entryY.getChangeType());
         /*   boolean xMatchesFileName = entryX.getNewFile().toLowerCase().endsWith(classFileName.toLowerCase());
            boolean yMatchesFileName = entryY.getNewFile().toLowerCase().endsWith(classFileName.toLowerCase());

            if (xMatchesFileName) {
                //TODO : Match with longest suffix
                if (yMatchesFileName) {
                    return Double.compare(entryX.getMatching(), entryY.getMatching()) * -1;
                } else {
                    return -1;
                }
            } else if (yMatchesFileName) {
                return 1;
            } else if (xIndex < 0) {
                return 1;
            } else if (yIndex < 0) {
                return -1;
            } else {
                return xIndex - yIndex;
            }*/
            return xIndex - yIndex;
        });
    }

    private boolean isEqualCardinalityMethodName(MethodMap childFileMap, MethodMap parentFileMap, MethodSourceInfo matchingMethodSourceInfo) {
        String targetMethodName = matchingMethodSourceInfo.getMethodDeclaration().getSignature().getName();
        long methodCountInChildFile = childFileMap.values()
                .stream()
                .filter(methodSourceInfo -> methodSourceInfo.getMethodDeclaration().getSignature().getName().equals(targetMethodName))
                .count();
        long methodCountInParentFile = parentFileMap.values()
                .stream()
                .filter(methodSourceInfo -> methodSourceInfo.getMethodDeclaration().getSignature().getName().equals(targetMethodName))
                .count();
        return methodCountInChildFile > 0 && methodCountInChildFile == methodCountInParentFile;
    }

    private Set<ChangeTag> detectChangeList(MethodHolder childMethodHolder, MethodHolder parentMethodHolder) {
        Set<ChangeTag> tagSet = new HashSet<>();
        MethodDeclaration matchingMethodDeclaration = parentMethodHolder.getMethodSourceInfo().getMethodDeclaration();

        if (!matchingMethodDeclaration.getJavadocComment()
                .equals(childMethodHolder.getMethodSourceInfo().getMethodDeclaration().getJavadocComment()) || Util.isDocumentationChange(matchingMethodDeclaration, childMethodHolder.getMethodSourceInfo().getMethodDeclaration())) {
            tagSet.add(ChangeTag.DOCUMENTATION);
        }

        if (!parentMethodHolder.getMethodSourceInfo().getAnnotation()
                .equals(childMethodHolder.getMethodSourceInfo().getAnnotation())) {
            tagSet.add(ChangeTag.ANNOTATION);
        }
        if (!parentMethodHolder.getMethodSourceInfo().getMethodRawSourceCode().equals(childMethodHolder.getMethodSourceInfo().getMethodRawSourceCode())) {
            if (!matchingMethodDeclaration.getNameAsString().equals(childMethodHolder.getMethodSourceInfo().getMethodDeclaration().getNameAsString())) {
                tagSet.add(ChangeTag.RENAME);
            }
            if (!matchingMethodDeclaration.getTypeAsString().equals(childMethodHolder.getMethodSourceInfo().getMethodDeclaration().getTypeAsString())) {
                tagSet.add(ChangeTag.RETURN_TYPE);
            }
            Ymodifiers modifiersX = Util.getInitialModifiers(matchingMethodDeclaration);
            Ymodifiers modifiersY = Util.getInitialModifiers(childMethodHolder.getMethodSourceInfo().getMethodDeclaration());
            if (!modifiersX.equals(modifiersY)) {
                tagSet.add(ChangeTag.MODIFIER);
            }

            List<Yparameter> parametersA = Util.getInitialParameters(matchingMethodDeclaration);
            List<Yparameter> parametersB = Util.getInitialParameters(childMethodHolder.getMethodSourceInfo().getMethodDeclaration());
            if (!parametersA.equals(parametersB)) {
                tagSet.add(ChangeTag.PARAMETER);
            }
            Yexceptions exceptionsA = Util.getInitialExceptions(matchingMethodDeclaration);
            Yexceptions exceptionsB = Util.getInitialExceptions(childMethodHolder.getMethodSourceInfo().getMethodDeclaration());
            if (!exceptionsA.equals(exceptionsB)) {
                tagSet.add(ChangeTag.EXCEPTION);
            }
            if (Util.getPrettyDeclarationAndBody(matchingMethodDeclaration).equals(Util.getPrettyDeclarationAndBody(childMethodHolder.getMethodSourceInfo().getMethodDeclaration()))) {
                tagSet.add(ChangeTag.FORMAT);
            }
            if (!Util.isBodyEqual(matchingMethodDeclaration, childMethodHolder.getMethodSourceInfo().getMethodDeclaration())) {
                tagSet.add(ChangeTag.BODY);
            }
        }
        if (parentMethodHolder.getFileChangeDto() != null) {

            if (jgitService.isFileExist(parentMethodHolder.getCommitHash(), childMethodHolder.getFile())
            || jgitService.isFileExist(childMethodHolder.getCommitHash(), parentMethodHolder.getFile())) {
                tagSet.add(ChangeTag.MOVE);
            } else {
                tagSet.add(ChangeTag.FILE_MOVE);
            }
        }
        return tagSet;
    }


    private MethodMap subtract(MethodMap setA, MethodMap setB) {
        /*if (setB != null) {
            return setA.entrySet().stream()
                    .filter(signatureAndMethodEntry -> !setB.containsKey(signatureAndMethodEntry.getKey()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
        return setA;*/

        MethodMap filteredMap = new MethodMap();
        for (MethodSourceInfo methodSourceInfo : setA.values()) {
            MethodDeclaration methodDeclaration = methodSourceInfo.getMethodDeclaration();
            if (setB.getBySignature(methodDeclaration) == null) {
                filteredMap.put(methodSourceInfo);
            }
        }
        return filteredMap;
    }

    private ChangeType getChangeTypeForFile(List<FileChangeDto> fileChangeList, String containerFilePath) {
        Optional<FileChangeDto> a = fileChangeList.stream()
                .filter(x -> x.getOldFile().equals(containerFilePath)).findAny();
        if (a.isEmpty()) {
            throw new IllegalStateException("this should not have been reached...");
        }
        return a.get().getChangeType();
    }

    private void removeFromModifiedFiles(Map<FileKey, ChangeType> modifiedFiles, String containerFilePath) {
        for (FileKey k : modifiedFiles.keySet()) {
            if (k.getFileName().equals(containerFilePath)) {
                modifiedFiles.remove(k);
                break;
            }
        }
    }

    public MethodHolder findBestMatchingCandidateInCommitModifiedFile(List<FileChangeDto> fileChangeList,
                                                                      String likelyParentCommitHash, MethodHolder currentMethodHolder) {

        double simThreshold = 0.75;
        MethodSourceInfo bestMatchCandidateMethod = null;
        double matchingCandidateMaxScore = 0.0;

        // TODO: sort the modified files by the closest match to the original file
        FileChangeDto bestModifiedFile = null;
        for (FileChangeDto fileChange : fileChangeList) {

            MethodMap applicableMethodMap;
            try {
                MethodMap likelyParentCommitMethodMap = javaParser.getAllMethodsInFile(likelyParentCommitHash, fileChange.getOldFile());
                applicableMethodMap = likelyParentCommitMethodMap;
                try {
                    MethodMap childCommitMethodMap = javaParser.getAllMethodsInFile(currentMethodHolder.getCommitHash(), fileChange.getOldFile());
                    applicableMethodMap = subtract(likelyParentCommitMethodMap, childCommitMethodMap);
                } catch (Exception ignored) {
                    log.info("Ignoring exception");
                }

            } catch (Exception e) {
                log.error("Failed to parse method from file {}", fileChange.getNewFile(), e);
                continue;
            }

            List<MethodSimilarity> methodSimilarityList;

            // get matched method by signature
            MethodSourceInfo matchingCandidate = applicableMethodMap
                    .getBySignature(currentMethodHolder.getMethodSourceInfo().getMethodDeclaration());
            // matched signature
          /*  if (matchingCandidate != null) {
                bestMatchCandidateMethod = matchingCandidate;
                bestModifiedFile = fileChange;
                break;
            } else*/
            {
                // no signature match, find a matched body
                if (!USE_SIM_HASH_BASED_MATCHING)
                    methodSimilarityList = MethodComparator.findBestMatchingWithJaroWinkler(applicableMethodMap,
                            currentMethodHolder.getMethodSourceInfo());
                else
                    methodSimilarityList = MethodComparator.findBestMatchingCandidateByMethodBody2(applicableMethodMap,
                            currentMethodHolder.getMethodSourceInfo());

                Optional<MethodSimilarity> optionalMethodSimilarity = MethodComparator.findTop(methodSimilarityList);

                if (optionalMethodSimilarity.isPresent()) {
                    MethodSimilarity methodSimilarity = optionalMethodSimilarity.get();

                    List<String> lineList = Util.toLineList(methodSimilarity.getMethodSourceInfo().getMethodRawSourceCode());
                    double overallTargetSimilarity;
                    if (lineList.size() < 4) {
                        overallTargetSimilarity = 1.0;
                    } else if (lineList.size() < 8) {
                        overallTargetSimilarity = 0.85;
                    } else {
                        overallTargetSimilarity = 0.75;
                    }
                    //TODO : define a linear formula
                    if (methodSimilarity.getOverallSimilarity() > matchingCandidateMaxScore && methodSimilarity.getOverallSimilarity() >= overallTargetSimilarity) {
                        bestMatchCandidateMethod = methodSimilarity.getMethodSourceInfo();
                        matchingCandidateMaxScore = methodSimilarity.getOverallSimilarity();
                        bestModifiedFile = fileChange;
                    }
                }
            }
        } // for

        if (bestModifiedFile != null) {
            log.info(bestModifiedFile.getOldFile());
            log.info(bestModifiedFile.getChangeType().name());
            log.info(likelyParentCommitHash);
            return MethodHolder.builder()
                    .commitHash(likelyParentCommitHash)
                    .file(bestModifiedFile.getOldFile())
                    .fileChangeDto(bestModifiedFile)
                    .methodSourceInfo(bestMatchCandidateMethod)
                    .build();
        } else {
            return null;
        }
    }

 /*   private void addChangeHistory(Method method, MethodHolder methodHolder, String parentCommitHash, Set<ChangeTag> changeTagList) {
        log.info("======== Adding Commit ========");
        String commitHash = methodHolder.getCommitHash();
        log.info("Added commit Hash {}", commitHash);
        log.info("Added change tag {}", Util.toChangeText(changeTagList));
        String containerFilePath = methodHolder.getFile();
        log.info("Added path {}", containerFilePath);
        log.info("========Commit Added =========");
        Commit commit = Commit.builder()
                .commitHash(commitHash)
                .parentCommitHash(parentCommitHash)
                .methodCode(methodHolder.getMethodSourceInfo().getMethodRawSourceCode())
                .documentation(Util.extractJavaDoc(methodHolder.getMethodSourceInfo().getMethodDeclaration()))
                .annotation(methodHolder.getMethodSourceInfo().getAnnotation())
                .methodContainerFile(containerFilePath)
                .commitInfo(jgitService.buildCommitInfo(commitHash))
                .changeTags(changeTagList)
                .build();
        method.addCommitInHistory(commit, changeTagList);
    }*/

}
