package rnd.git.history.finder.hash;

public interface IRegularHashGenerator {

	long generate64BitHashFor(String key);

	long generate64BitHashFor(String key, long seed);

}