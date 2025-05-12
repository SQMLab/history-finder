package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.*;
@NoArgsConstructor
public class GraphImpl implements Graph {
    private String source;
    private final Map<String, Set<String>> adjacencyList = new HashMap<>();
    private final Set<String> nodeList = new HashSet<>();

    @Override
    public void setSourceNode(String source) {
        this.source = source;
    }

    @Override
    public String getSourceNode() {
        return source;
    }

    @Override
    public void addNode(String u) {
        nodeList.add(u);
    }

    @Override
    public void addEdge(String u, String v) {
        addNode(u);
        addNode(v);
        if (!adjacencyList.containsKey(u)){
            adjacencyList.put(u, new LinkedHashSet<>());
        }
        adjacencyList.get(u).add(v);
    }

    @Override
    public Set<String> getNodeSet() {
        return nodeList;
    }

    @Override
    public Set<String> getParentList(String u) {
        if (adjacencyList.containsKey(u)){
            return adjacencyList.get(u);
        }else {
            return Set.of();
        }
    }
}
