package rnd.git.history.finder.jgit;

import info.debatty.java.stringsimilarity.JaroWinkler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import rnd.git.history.finder.Util;
import rnd.git.history.finder.dto.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class JgitService {

    private Repository repository;
    private Git git;
    private DiffCollector diffCollector;

    static JaroWinkler jaroWinkler = new JaroWinkler();

    public JgitService(String cacheDirectory, String repositoryUrl, String repositoryName) {

        git = cloneIfNeeded(cacheDirectory + File.separator + repositoryName, repositoryUrl, null, null);
        repository = git.getRepository();

    }


    public String getHeadCommitHash() throws IOException {
        return repository.resolve(Constants.HEAD).getName();
    }
    public String getFileContent(FileHistory fileHistory) throws IOException {
        return getFileContent(fileHistory.getCommit(), fileHistory.getPath());
    }

    // file must be relative to the actual repo (e.g., from src/)
    public String getFileContent(String commitHash, String file) throws IOException {
        return new String(readFileContentByte(commitHash, file), StandardCharsets.UTF_8);
    }

    public byte[] readFileContentByte(String commitHash, String file) throws IOException {
        // source:
        // https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java

//		System.out.println("getting " + file +" from " +commitHash);

        ObjectId lastCommitId = repository.resolve(commitHash);

        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(lastCommitId);
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(file));
                if (!treeWalk.next()) {
                    throw new FileNotFoundException("Did not find expected file: " + file);
                }
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                revWalk.dispose();
                // and then one can the loader to read the file
                return loader.getBytes();
            }
        }
    }

    public boolean isFileExist(String commitHash, String file) {
        // source:
        // https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java

//		System.out.println("getting " + file +" from " +commitHash);
        try {
            ObjectId lastCommitId = repository.resolve(commitHash);

            // a RevWalk allows to walk over commits based on some filtering that is defined
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(lastCommitId);
                // and using commit's tree find the path
                RevTree tree = commit.getTree();
                // now try to find a specific file
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(file));
                    return treeWalk.next();
                }
            }
        } catch (IOException ioException) {
            return false;
        }
    }

    public List<String> getFileContent2(String commitString, String path) {
//		System.out.println("getting " + path +" from " +commitString);

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(commitString));
            try (TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
                treeWalk.setRecursive(true);
//					if (!treeWalk.next()) {
//						throw new IllegalStateException("Did not find expected file");
//					}
                ObjectId blobId = treeWalk.getObjectId(0);
                try (ObjectReader objectReader = repository.newObjectReader()) {
                    ObjectLoader objectLoader = objectReader.open(blobId);
//						byte[] bytes = objectLoader.getBytes();

                    List<String> lines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(objectLoader.openStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return lines;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Did not find expected file " + e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public RevCommit getRevCommit(String commitHash) {
        RevWalk revWalk = new RevWalk(repository);

        try {
            return revWalk.parseCommit(repository.resolve(commitHash));
        } catch (IOException e) {
            log.error("Failed to resolve commit {}", commitHash, e);
            throw new RuntimeException(e);
        }
    }

    // thre should be a better way in codeshovel or codetracker.
    // source:
    // https://stackoverflow.com/questions/11471836/how-to-git-log-follow-path-in-jgit-to-retrieve-the-full-history-includi
    public List<FileHistory> listFileChangeHistory(String file, String startCommitHash) throws IOException, MissingObjectException, GitAPIException {
        List<RevCommit> commits = new ArrayList<RevCommit>();
        List<FileHistory> fileHistories = new ArrayList<>();
        RevCommit start = null;
        do {
            Iterable<RevCommit> iterableLog = git.log().add(createCommitObjectId(startCommitHash)).addPath(file).call();
            int ct = 0;
            for (RevCommit commit : iterableLog) {
                if (commits.contains(commit)) {
                    start = null;
                } else {
                    start = commit;
                    commits.add(commit);
                    fileHistories.add(FileHistory.builder().commit(commit.getName()).path(file).committedAt(new Date(commit.getCommitTime() * 1000L)).build());
                    log.info("{} add to fileHistories {}", ++ct, commit.getName());
                }
            }
            if (start == null) return fileHistories;

        } while ((file = getRenamedPath(start, git, repository, file)) != null);

        return fileHistories;
    }

    // https://stackoverflow.com/questions/11471836/how-to-git-log-follow-path-in-jgit-to-retrieve-the-full-history-includi
    private String getRenamedPath(RevCommit start, Git git, Repository repository, String path) throws IOException, MissingObjectException, GitAPIException {
//		System.out.println("called getRenamedPath for: " + start.toString());

//    	Iterable<RevCommit> allCommitsLater = git.log().add(start).call();  //try .setMaxCount(2) https://gist.github.com/wobu/ccfaccfc6c04c02b8d1227a0ac151c36
        Iterable<RevCommit> allCommitsLater = git.log().add(start).setMaxCount(2).call(); // try .setMaxCount(2)
        // https://gist.github.com/wobu/ccfaccfc6c04c02b8d1227a0ac151c36

        for (RevCommit commit : allCommitsLater) {
//        	System.out.println("allCommitsLater" + commit.toString());
            TreeWalk tw = new TreeWalk(repository);

            tw.addTree(commit.getTree());
            tw.addTree(start.getTree());
            tw.setRecursive(true);

//su          check performance of this
            tw.setFilter(TreeFilter.ANY_DIFF);

            RenameDetector rd = new RenameDetector(repository);
            rd.addAll(DiffEntry.scan(tw));

//su            try this for performance // https://gist.github.com/wobu/ccfaccfc6c04c02b8d1227a0ac151c36
            rd.setRenameScore(50);

            List<DiffEntry> files = rd.compute();
            for (DiffEntry diffEntry : files) {
                if ((diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME || diffEntry.getChangeType() == DiffEntry.ChangeType.COPY) && diffEntry.getNewPath().contains(path)) {
//					System.out.println("Found: " + diffEntry.toString() + " return " + diffEntry.getOldPath());
                    return diffEntry.getOldPath();
                }
            }
        }
        return null;
    }


    public String getRenamedPath(RevCommit startCommit, String filePath) throws MissingObjectException, IOException, GitAPIException {
        return getRenamedPath(startCommit, git, repository, filePath);
    }


    @SneakyThrows
    public ArrayList<FileHistory> listFileChangeHistoryRegardlessRename(String path, String startCommit) {
        ArrayList<FileHistory> fileHistories = new ArrayList<>();
        git.log()
                .add(createCommitObjectId(startCommit))
                .addPath(path)
                .call()
                .forEach(commit -> fileHistories.add(FileHistory.builder()
                        .commit(commit.getName())
                        .path(path)
                        .committedAt(new Date(commit.getCommitTime() * 1000L))
                        .build()));
        return fileHistories;
    }


    @SneakyThrows
    public List<CommitNode> gitLogFile(String path, String startCommit) {
        List<CommitNode> commitNodeList = new ArrayList<>();
        git.log()
                .add(createCommitObjectId(startCommit))
                .addPath(path)
                .call()
                .forEach(revCommit -> commitNodeList.add(Util.toCommitNode(revCommit)));
        return commitNodeList;
    }

    @SneakyThrows
    public Graph gitLogFileAsGraph(String path, String startCommit) {
        List<CommitNode> commitNodeList = new ArrayList<>();
        Graph graph = new GraphImpl();
        AtomicReference<String> firstChangeCommitHash = new AtomicReference<>();

        git.log()
                .add(createCommitObjectId(startCommit))
                .addPath(path)
                .call()
                .forEach(revCommit -> {
                    String childCommitHash = revCommit.getName();
                    if (firstChangeCommitHash.get() == null) {
                        firstChangeCommitHash.set(childCommitHash);
                    }
                    graph.addNode(childCommitHash);
                    for (RevCommit parent : revCommit.getParents()) {
                        graph.addEdge(childCommitHash, parent.getName());
                    }
                });
        graph.setSourceNode(startCommit);
        if (firstChangeCommitHash.get() != null) {
            graph.addEdge(startCommit, firstChangeCommitHash.get());
        }
        return graph;
    }

    private ObjectId createCommitObjectId(String startCommit) throws IOException {
        if ("HEAD".equalsIgnoreCase(startCommit))  {
            return repository.resolve(Constants.HEAD);
        }else{
            return ObjectId.fromString(startCommit);
        }
    }


    private class DiffCollector extends RenameCallback {
        List<DiffEntry> diffs = new ArrayList<DiffEntry>();

        @Override
        public void renamed(DiffEntry diff) {
//       	    System.out.println("diffCollector :: adding diff "+ diff.getOldId() + diff.getNewId());
            diffs.add(diff);
        }
    }


    public ArrayList<FileHistory> showFileHistoryBeforeRename(String filepath, String startCommit) {
        ArrayList<FileHistory> fileHistories = new ArrayList<>();
        try {
            Config config = repository.getConfig();
            config.setBoolean("diff", null, "renames", true);

            RevWalk rw = new RevWalk(repository);
            diffCollector = new DiffCollector();

            org.eclipse.jgit.diff.DiffConfig dc = config.get(org.eclipse.jgit.diff.DiffConfig.KEY);
            FollowFilter followFilter = FollowFilter.create(filepath, dc);

            followFilter.setRenameCallback(diffCollector);
            rw.setTreeFilter(followFilter);
            rw.markStart(rw.parseCommit(repository.resolve(startCommit)));

            RevCommit commit = null;
            while ((commit = rw.next()) != null) {
//				System.out.println("diffs :" + diffCollector.diffs.size());
                System.out.println("add to fileHistories " + commit.getName());
                fileHistories.add(FileHistory.builder().commit(commit.getName()).path(filepath).committedAt(new Date(commit.getCommitTime() * 1000L)).build());
            }

//			for (RevCommit c : rw) {
//				System.out.println(">>>" + c.toString());
//			}

            rw.close();
            rw.dispose();

//			for (DiffEntry d : diffCollector.diffs) {
//				System.out.println("diffCollector :: type: " + d.getChangeType() + "\n new: " + d.getNewPath()
//						+ "\n old: " + d.getOldPath());
//			}

        } catch (Exception e) {
            // TODO: handle exception
        }
        return fileHistories;
    }


    public List<String> findModifiedFiles(String currentCommit, String oldCommit) throws IOException {
        // https://stackoverflow.com/questions/28785364/list-of-files-changed-between-commits-with-jgit

        List<String> modifiedfiles = new ArrayList<>();
        ObjectReader reader = git.getRepository().newObjectReader();

        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        ObjectId oldTree = git.getRepository().resolve(currentCommit + "^{tree}");
        oldTreeIter.reset(reader, oldTree);

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        ObjectId newTree = git.getRepository().resolve(oldCommit + "^{tree}");
        newTreeIter.reset(reader, newTree);

        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(git.getRepository());
        List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

        for (DiffEntry entry : entries) {
            if (!entry.getChangeType().toString().equals("DELETE")) {
                if (entry.getNewPath().toString().endsWith(".java")) {
                    modifiedfiles.add(entry.getNewPath().toString());
                }
            }
        }

        diffFormatter.close();
        return modifiedfiles;
    }


    public static class FileKey implements Comparable<FileKey> {
        private String fileName;
        private double matchIndex;

        public String getFileName() {
            return fileName;
        }

        public double getMatchIndex() {
            return matchIndex;
        }

        public FileKey(String fileName, double matchIndex) {
            super();
            this.fileName = fileName;
            this.matchIndex = matchIndex;
        }

        @Override
        public int compareTo(FileKey o) {
            if (matchIndex == o.matchIndex) return this.fileName.compareTo(o.fileName) * -1;
            return Double.compare(matchIndex, o.matchIndex) * -1;
        }
    }

    // collected ModType
    /** ADD a new file to the project */
    /** MODIFY an existing file in the project (content and/or mode) */
    /** RENAME an existing file to a new location */
    /**
     * COPY an existing file to a new location, keeping the original
     */
    public List<FileChangeDto> findModifiedFilesWithModType(String childCommitHash, String parentCommitHash, String currentFile) {
        // https://stackoverflow.com/questions/28785364/list-of-files-changed-between-commits-with-jgit
//		System.out.println("looking for file changed between: <" + childCommitHash + "> and <"+ parentCommitHash + ">");

        List<FileChangeDto> fileChangeList = new ArrayList<>();

        Set<String> setA = Arrays.stream(currentFile.split("/"))
                .flatMap(s -> Arrays.stream(s.split("(?=\\p{Lu})")))
                .collect(Collectors.toSet());

        int lenA = setA.size();

        ObjectReader reader = git.getRepository().newObjectReader();

        CanonicalTreeParser childTreeIterator = new CanonicalTreeParser();
        try {
            ObjectId childCommitTree = git.getRepository().resolve(childCommitHash + "^{tree}");
            //childTreeIterator.reset(reader, childCommitTree);
            childTreeIterator.reset(reader, getRevCommit(childCommitHash).getTree());

            CanonicalTreeParser parentTreeIter = new CanonicalTreeParser();
            ObjectId parentCommitTree = git.getRepository().resolve(parentCommitHash + "^{tree}");
            //parentTreeIter.reset(reader, parentCommitTree);
            parentTreeIter.reset(reader, getRevCommit(parentCommitHash).getTree());

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setDetectRenames(true); // added
            diffFormatter.setPathFilter(PathSuffixFilter.create(".java"));
            List<DiffEntry> entries = diffFormatter.scan(parentTreeIter, childTreeIterator);
            diffFormatter.close();
            // we are not interested in ADD CHANGE TYPE,
//*		 we do need DELETE as the candidate method might have moved from the file in old commit
//*		 that is now deleted in the current commit
            for (DiffEntry entry : entries
//				.stream()
//				.filter(e -> !e.getChangeType().equals(ChangeType.DELETE))
//				.toList()
            ) {
                //we need the path from parentCommitHash so that we can grab the original file that copy/renamed,
                //modified file ones are with same name in both

//			Set<String> setB = new HashSet<String>(Arrays.asList(entry.getNewPath().toString().split("/")));

           /*     if (entry.getChangeType().equals(ChangeType.DELETE)) {

                    continue;
                }*/

                Set<String> setB = Arrays.stream(entry.getNewPath().split("/"))
                        .flatMap(s -> Arrays.stream(s.split("(?=\\p{Lu})")))
                        .collect(Collectors.toSet());

                Set<String> intersectSet = setA.stream().filter(setB::contains).collect(Collectors.toSet());

                double idx = (double) intersectSet.size() / lenA;

                fileChangeList.add(FileChangeDto.builder()
                        .oldFile(entry.getOldPath())
                        .newFile(entry.getNewPath())
                        .matching(idx)
                        .changeType(entry.getChangeType())
                        .build());

            }

            return fileChangeList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, RevCommit> getAllLinkedCommits(String commitId) throws IOException, GitAPIException {
        Map<String, RevCommit> linkedCommits = new HashMap<>();
        Iterable<RevCommit> revCommits;
        revCommits = git.log().add(repository.resolve(commitId)).call();
        RevCommit prev = null;
        for (RevCommit revCommit : revCommits) {
            linkedCommits.put(revCommit.getName(), prev);
            prev = revCommit;
        }
        return linkedCommits;
    }

    private Git cloneIfNeeded(String projectPath, String url, String username, String secret) {
        try {
            File rootDirectory = new File(projectPath);
            if (rootDirectory.exists()) {
                Optional<String> dotGitFile = Arrays.stream(rootDirectory.list()).filter(file -> file.equalsIgnoreCase(".git")).findAny();
                return new Git(new RepositoryBuilder().setGitDir(dotGitFile.isPresent() ? new File(rootDirectory, ".git") : rootDirectory).readEnvironment().findGitDir().build());

            } else {
                return Git.cloneRepository().setDirectory(rootDirectory).setURI(url).setCloneAllBranches(true).setCredentialsProvider(username != null && secret != null ? new UsernamePasswordCredentialsProvider(username, secret) : null).call();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public CommitInfo buildCommitInfo(String commitName) {
        RevCommit revCommit = getRevCommit(commitName);
        return new CommitInfo(revCommit.getAuthorIdent().getName(), revCommit.getAuthorIdent().getEmailAddress(), revCommit.getCommitTime());

    }

}
