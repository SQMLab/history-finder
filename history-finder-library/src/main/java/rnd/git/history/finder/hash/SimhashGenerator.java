package rnd.git.history.finder.hash;

import rnd.git.history.finder.hash.token.DefaultTokenBuilder;
import rnd.git.history.finder.hash.token.ITokenBuilder;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class SimhashGenerator {
	private ITokenBuilder tokenBuilder;
	private IRegularHashGenerator regularHashGenerator;

	public SimhashGenerator(){
		tokenBuilder = new DefaultTokenBuilder();
		regularHashGenerator = new DefaultRegularHashGenerator();
	}
	
	public long[] generateSimhash(MethodSourceInfo codeFragment){
		int v1[]= new int [64];
		int v2[]= new int [64];
		
		//int seconderyHashMinFreq = ? 1 : 0;
		
		Multiset<String> tokenMap = HashMultiset.create(tokenBuilder.generateToken(codeFragment));
		
		/*
		System.out.println("########################");
		for(Entry<String, Short> entry: tokenMap.entrySet()){
			System.out.println(entry.getValue()+" : " + hash+" : " + entry.getKey());
		}
		System.out.println("########################\n");
		*/
		
		for (Multiset.Entry<String> token : tokenMap.entrySet()) {

			int originalTokenFrequency = token.getCount();
			int modifiedTokenFrequency = originalTokenFrequency;
			// HashFactory.buildLookup3ycs64Hash(token.getElement());
			
//			if (Environment.isTokenFrequencyNormalizationOn()) {
//				int normalizationThreshold = Environment.getTokenFrequencyNormalizationThreshold();
//				int valueOverThreshold = Environment.getTokenFrequencyNormalizationOverThresholdValue();
//				modifiedTokenFrequency = originalTokenFrequency > normalizationThreshold ? valueOverThreshold
//						: originalTokenFrequency;
//			}
			
			long tokenHash = regularHashGenerator.generate64BitHashFor(token.getElement());
			for (int c = 0; c < 64; c++) {
				v1[c] += (tokenHash & (1l << c)) == 0 ? -modifiedTokenFrequency : modifiedTokenFrequency;
			}

			// secondary hash for better precision
			// if(originalTokenFrequency > seconderyHashMinFreq) {
			tokenHash = regularHashGenerator.generate64BitHashFor(token.getElement(), 32767l);
			for (int c = 0; c < 64; c++) {
				v2[c] += (tokenHash & (1l << c)) == 0 ? -modifiedTokenFrequency : modifiedTokenFrequency;
			}
			// }
		}

		long simhash[] = { 0, 0 };
		for (int c = 0; c < 64; c++)
			simhash[0] |= (v1[c] > 0 ? 1l : 0l) << c;

		for (int c = 0; c < 64; c++)
			simhash[1] |= (v2[c] > 0 ? 1l : 0l) << c;
		
		return simhash;
	}

}