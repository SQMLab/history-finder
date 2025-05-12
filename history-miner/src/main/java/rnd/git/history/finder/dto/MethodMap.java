package rnd.git.history.finder.dto;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.LinkedListMultimap;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 15/6/24
 **/
public class MethodMap {
    private final LinkedListMultimap<String, MethodSourceInfo> linkedListMultimap = LinkedListMultimap.create();


    public MethodSourceInfo getOneByMethodName(String methodName) {
        List<MethodSourceInfo> methodSourceInfoList = linkedListMultimap.get(methodName);
        if (!methodSourceInfoList.isEmpty()) {
            return methodSourceInfoList.getFirst();
        }
        return null;
    }

    public List<MethodSourceInfo> getAllByMethodName(String methodName) {
        return linkedListMultimap.get(methodName);
    }

    public MethodSourceInfo getBySignature(MethodDeclaration methodDeclaration) {

        String signature = methodDeclaration.getSignature().asString();
        String fullSignature = methodDeclaration.getDeclarationAsString();

        List<MethodSourceInfo> methodSourceInfoList = linkedListMultimap.get(methodDeclaration.getNameAsString());
        return methodSourceInfoList
                .stream()
                .filter(methodSourceInfo -> fullSignature.equals(methodSourceInfo.getMethodDeclaration().getDeclarationAsString()))
                .findFirst()
                .orElse(methodSourceInfoList.stream()
                        .filter(methodSourceInfo -> signature.equals(methodSourceInfo.getMethodDeclaration().getSignature().asString()))
                        .findFirst()
                        .orElse(null));

    }


    public Set<String> keySet() {
        return linkedListMultimap.keySet();
    }

    public void put(MethodSourceInfo methodSourceInfo) {
        linkedListMultimap.put(methodSourceInfo.getMethodDeclaration().getNameAsString(), methodSourceInfo);
    }

    public boolean putAll(String key, List<MethodSourceInfo> methodSourceInfoList) {
        return linkedListMultimap.putAll(key, methodSourceInfoList);
    }

    public List<MethodSourceInfo> values() {
        return linkedListMultimap.values();
    }

    public int size() {
        //TODO : might need to sum up all elements
        return linkedListMultimap.size();
    }
}
