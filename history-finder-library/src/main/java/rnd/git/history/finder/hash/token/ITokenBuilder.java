package rnd.git.history.finder.hash.token;

import java.util.Collection;

import rnd.git.history.finder.hash.token.filter.ITokenFilter;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

public interface ITokenBuilder {

	public Collection<String> generateToken(MethodSourceInfo codeFragment);

	public void setFilterChain(ITokenFilter tokenFilter);

}