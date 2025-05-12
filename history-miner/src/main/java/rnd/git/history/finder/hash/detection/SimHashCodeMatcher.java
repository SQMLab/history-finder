package rnd.git.history.finder.hash.detection;


import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import rnd.git.history.finder.dto.MethodMap;
import rnd.git.history.finder.dto.MethodSimilarity;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

public class SimHashCodeMatcher{
	
	private static Map<Integer, Integer> syncedSimthreshold2Map = new HashMap<Integer, Integer>(8);
	
	public static final SimHashCodeMatcher INSTANCE = new SimHashCodeMatcher();
	
	static {
	syncedSimthreshold2Map.put(6, 5);
	syncedSimthreshold2Map.put(7, 6);
	syncedSimthreshold2Map.put(8, 7);
	syncedSimthreshold2Map.put(9, 8);
	syncedSimthreshold2Map.put(10, 8);
	syncedSimthreshold2Map.put(11, 9);
	syncedSimthreshold2Map.put(12, 12);
	syncedSimthreshold2Map.put(13, 13);
	}
	
	private SimHashCodeMatcher() {}
	
	public List<MethodSimilarity> findBestMatchingCandidateWithScore(MethodSourceInfo searchItem,
			MethodMap allMth) {
	
		int deviation = 0; 
		int simThreshold1 = 13;
//		int simThreshold2 = syncedSimthreshold2Map.get(simThreshold1);
		int dynamicSimThreshold1;// = simThreshold + deviation;
		//int dynamicSimThreshold2;// = simThreshold2 + deviation;
		
		int searchItemLoc = searchItem.getEndLine() - searchItem.getStartLine() + 1;
		
		deviation = getThresholDeviationValue(searchItemLoc, simThreshold1);
		
		dynamicSimThreshold1 = simThreshold1 + deviation;
		//dynamicSimThreshold2 = simThreshold2 + deviation;
		
//		List<MethodSourceInfo> neighbors = new ArrayList<MethodSourceInfo>();
		
		
		MethodSourceInfo bestMatchCandidate = null;
		int minHam = 13;
		int currentHam;
		List<MethodSimilarity> methodSimilarityList = new ArrayList<>();
		for(MethodSourceInfo matchCandidate : allMth.values()){
			currentHam = hamming_dist(searchItem.getSimHash1(), matchCandidate.getSimHash1());
			//int hamming_dist2 = hamming_dist(searchItem.getSimHash2(), matchCandidate.getSimHash2());
			
			if(currentHam <= dynamicSimThreshold1) { //&& hamming_dist2 <= dynamicSimThreshold2){
				if ( currentHam < minHam){
					minHam = currentHam;
					bestMatchCandidate = matchCandidate;
				}
			}
			methodSimilarityList.add(MethodSimilarity.builder()
					.methodSourceInfo(matchCandidate)
					.overallSimilarity(1.0 - currentHam / dynamicSimThreshold1)
					.build());
		}

		return methodSimilarityList;
//		return bestMatchCandidate;
		//return new AbstractMap.SimpleEntry<MethodSourceInfo, Double>(bestMatchCandidate, ( 1.0  - minHam/dynamicSimThreshold1 ) );
	}

	private int getThresholDeviationValue(int searchItemLoc, int simThreshold1) {
		
		int deviation = 0;
		
		switch(simThreshold1){
		
		case 6:
			///simThreshold2 = 5;
			break;
		
		case 7:
			if(searchItemLoc < 6){
				deviation = -1;
			}else if(searchItemLoc < 8){
				deviation = -1;
			}
			///simThreshold2 = 6;
			break;
		
		case 8:
			if(searchItemLoc < 6){
				deviation = -2;
			}else if(searchItemLoc < 8){
				deviation = -1;
			}
			///simThreshold2 = 7;
			break;
		
		case 9:
			if(searchItemLoc < 6){
				deviation = -3;
			}else if(searchItemLoc < 8){
				deviation = -2;
			}else if(searchItemLoc < 10){
				deviation = -1;
			}
			///simThreshold2 = 8;
			break;
		
		case 10:
			if(searchItemLoc < 6){
				deviation = -3;
			}else if(searchItemLoc < 8){
				deviation = -2;
			}else if(searchItemLoc < 10){
				deviation = -2;
			}else if(searchItemLoc < 20){
				deviation = -1;
			}
			///simThreshold2 = 8;
			break;
		
		case 11:
			if(searchItemLoc < 6){
				deviation = -4;
			}else if(searchItemLoc < 8){
				deviation = -3;
			}else if(searchItemLoc < 10){
				deviation = -2;
			}else if(searchItemLoc < 20){
				deviation = -1;
			}
			///simThreshold2 = 9;
			break;
		
		case 12:
			if(searchItemLoc < 6){
				deviation = -4;
			}else if(searchItemLoc < 8){
				deviation = -3;
			}else if(searchItemLoc < 10){
				deviation = -3;
			}else if(searchItemLoc < 20){
				deviation = -2;
			}else if(searchItemLoc < 30){
				deviation = -1;
			}	
			///simThreshold2 = 12;
			break;
		case 13:
			if(searchItemLoc < 6){
				deviation = -4;
			}else if(searchItemLoc < 8){
				deviation = -3;
			}else if(searchItemLoc < 10){
				deviation = -3;
			}else if(searchItemLoc < 20){
				deviation = -2;
			}else if(searchItemLoc < 30){
				deviation = -1;
			}	
			///simThreshold2 = 13;
			break;
		}
		
		/*else if(item.lineOfCode > 40){
			deviation = 2;
		}else if(item.lineOfCode > 30){
			deviation = 1;
		}*/
		return deviation;
	}
	
	private int hamming_dist(Long simhash1, Long simhash2) {
		return Long.bitCount(simhash1 ^ simhash2);
	}
}
