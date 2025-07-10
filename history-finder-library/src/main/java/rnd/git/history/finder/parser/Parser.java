package rnd.git.history.finder.parser;

import rnd.git.history.finder.dto.Method;
import rnd.git.history.finder.dto.MethodHolder;

public interface Parser {
    Method retrieveGivenMethodFromFile(String commitHash, String file, String methodName, Integer startLine);

    Boolean IsTheIdenticalMethodHere(String methodCode, String filePath, String commitName);
    MethodHolder findMethod(String commitHash, String file, String methodName, Integer startLine);
    MethodHolder findMethod(String commitHash, String file, String methodName, String fullMethodSignature);
}
