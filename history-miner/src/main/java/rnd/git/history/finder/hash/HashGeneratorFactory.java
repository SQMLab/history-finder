package rnd.git.history.finder.hash;

public class HashGeneratorFactory {

	public static IRegularHashGenerator LoadRegularHashgenerator() {
			return new DefaultRegularHashGenerator();
	}
}
