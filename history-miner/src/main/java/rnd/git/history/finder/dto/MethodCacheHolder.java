package rnd.git.history.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shahidul Islam
 * @since 29/5/24
 **/
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MethodCacheHolder {
    @Builder.Default
    private Map<String, Map<String, Map<String, MethodSourceInfo>>> methodMapping = new HashMap<>();

    public Map<String, MethodSourceInfo> findMethodSignatureMapping(String commitHash, String file) {
        if (methodMapping.containsKey(commitHash)) {
            Map<String, Map<String, MethodSourceInfo>> fileToMethodMapping = methodMapping.get(commitHash);
            if (fileToMethodMapping.containsKey(file)) {
                return fileToMethodMapping.get(file);
            }
        }
        return null;
    }
    public void putMethodMapping(String commitHash, String file, Map<String, MethodSourceInfo> methodSourceInfoMap){
        if (!methodMapping.containsKey(commitHash)){
            methodMapping.put(commitHash, new HashMap<>());
            methodMapping.get(commitHash).put(file, methodSourceInfoMap);
        }
    }
}
