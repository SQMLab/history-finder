package rnd.git.history.finder.algortihm;

import org.eclipse.jgit.api.errors.GitAPIException;
import rnd.git.history.finder.dto.Method;

import java.io.IOException;

public interface Algorithm {

    void compute(Method method) throws GitAPIException, IOException;
}
