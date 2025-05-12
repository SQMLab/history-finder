package rnd.git.history.finder.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rnd.git.history.finder.Util;

@Data
public class Method {
    @Deprecated
    private List<Commit> changeCommitHistory;
    @Deprecated
    private List<String> changeMadeHistory;
    @Deprecated
    private MethodHolder methodHolder;
    private List<HistoryEntry> historyEntryList;
    Integer analyzedCommitCount;
    String methodId;
    public Method() {
        changeCommitHistory = new ArrayList<>();
        changeMadeHistory = new ArrayList<>();
        historyEntryList = new ArrayList<>();
        analyzedCommitCount = 0;
    }

    public Method(MethodHolder methodHolder) {
        this();
        this.methodHolder = methodHolder;
        this.methodId = Util.getInitialId(methodHolder.getMethodSourceInfo().getMethodDeclaration());
    }

    public void addCommitInHistory(Commit commit, Set<ChangeTag> changeTags) {
        String changeText = Util.toChangeText(changeTags);
        //if (!StringUtils.isEmptyOrNull(changeText)) {
            changeCommitHistory.add(commit);
            changeMadeHistory.add(changeText);
        //}
    }

    public void addHistoryEntry(HistoryEntry historyEntry){
        historyEntryList.add(historyEntry);
    }

    public List<Commit> getChangeHistory() {
        return changeCommitHistory;
    }

    public void appendChangeTag(ChangeTag changeTag){
        changeMadeHistory.set(changeMadeHistory.size() - 1, changeMadeHistory.getLast() + (changeMadeHistory.getLast().isBlank() ? "" : "/") + changeTag.getCode());
    }

}
