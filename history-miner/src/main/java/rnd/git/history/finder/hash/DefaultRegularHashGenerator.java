package rnd.git.history.finder.hash;

import org.apache.solr.common.util.Hash;



public class DefaultRegularHashGenerator implements IRegularHashGenerator {

	/* (non-Javadoc)
	 * @see ca.usask.cs.srlab.simcad.hash.IRegularHashGenerator#generate64BitHashFor(java.lang.String)
	 */
	@Override
	public long generate64BitHashFor(String key) {
		return generate64BitHashFor(key, 0);
	}

	/* (non-Javadoc)
	 * @see ca.usask.cs.srlab.simcad.hash.IRegularHashGenerator#generate64BitHashFor(java.lang.String, long)
	 */
	@Override
	public long generate64BitHashFor(String key, long seed) {

		/*
		// LZ4-JAVA	
		byte[] data;
		try {
			data = key.getBytes("UTF-8");
		XXHashFactory factory = XXHashFactory.fastestInstance();
		XXHash64 xxHash64  = factory. hash64();
		xxHash64.hash(data, 0, data.length, seed);
		
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
		//zero allocation hash
		//XXH3
		LongHashFunction hfXXH3 = LongHashFunction.xx3(seed);
		hfXXH3.hashChars(key, 0, key.length());
		//XxHash
		LongHashFunction hfXXH = LongHashFunction.xx(seed);
		hfXXH.hashChars(key, 0, key.length());
		*/
		
		return Hash.lookup3ycs64(key, 0, key.length(), seed);
	}
}
