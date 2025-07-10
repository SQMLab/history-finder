package rnd.git.history.finder.parser.implementation;

import rnd.git.history.finder.dto.Method;
import rnd.git.history.finder.dto.MethodHolder;
import rnd.git.history.finder.parser.Parser;

public class PythonParser implements Parser {
    @Override
    public Method retrieveGivenMethodFromFile(String commitHash, String file, String methodName, Integer startLine) {
        return null;
    }

    @Override
    public Boolean IsTheIdenticalMethodHere(String methodCode, String filePath, String commitName) {
        return null;
    }

    @Override
    public MethodHolder findMethod(String commitHash, String file, String methodName, Integer startLine) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public MethodHolder findMethod(String commitHash, String file, String methodName, String fullMethodSignature) {
        throw new RuntimeException("Not implemented yet");
    }
}
