package rnd.git.history.finder.dto;

public class CommitInfo {
    String authorName;
    String authorEmail;
    int time;
    String methodContainingFile;
    public CommitInfo(String authorName, String authorEmail, int time){
        this.authorName = authorName;
        this.time = time;
        this.authorEmail = authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }


}
