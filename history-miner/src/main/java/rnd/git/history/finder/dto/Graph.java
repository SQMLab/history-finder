package rnd.git.history.finder.dto;

import java.util.List;
import java.util.Set;

public interface Graph {
    void setSourceNode(String source);
    String getSourceNode();
    void addNode(String u);
    void addEdge(String u, String v);
    Set<String> getNodeSet();
    Set<String> getParentList(String u);

}
