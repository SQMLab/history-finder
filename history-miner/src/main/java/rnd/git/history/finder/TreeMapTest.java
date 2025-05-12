package rnd.git.history.finder;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class TreeMapTest {

	
	
	static class FileKey implements Comparable<FileKey>{
		String fileName;
		double matchIndex;
		public FileKey(String fileName, double matchIndex) {
			super();
			this.fileName = fileName;
			this.matchIndex = matchIndex;
		}
		@Override
		public int compareTo(FileKey o) {
			if (matchIndex == o.matchIndex)
				return this.fileName.compareTo(o.fileName) * -1;
			return Double.compare(matchIndex, o.matchIndex) * -1;
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s1 = "spring-boot-actuator/src/main/java/org/springframework/boot/actuate/metrics/jdbc/TomcatDataSourceMetadata.java";
		String[] sx = new String[3];
		 sx[0] = "spring-boot-actuator/src/main/java/org/springframework/boot/actuate/autoconfigure/HealthIndicatorAutoConfiguration.java";
		 sx[1] = "spring-boot-actuator/src/main/java/org/springframework/boot/actuate/autoconfigure/HealthIndicatorAutoConfiguration.java";
		 sx[2] = "spring-boot-actuator/src/main/java/org/springframework/boot/actuate/autoconfigure/HealthIndicatorAutoConfiguration.java";
		 
		 
//		 sx[1] = "spring-boot-actuator/src/main/java/org/springframework/boot/actuate/endpoint/DataSourcePublicMetrics.java";
//		 sx[2] = "spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/HikariDataSourceMetadata.java";
//		 sx[3] = "spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/CompositeDataSourceMetadataProvider.java";
//		 sx[4] = "spring-boot-actuator/src/main/java/org/springframework/boot/actuate/autoconfigure/DataSourceMetricsAutoConfiguration.java";
//		 sx[5] = "spring-boot-actuator/src/test/java/org/springframework/boot/actuate/autoconfigure/HealthIndicatorAutoConfigurationTests.java";
//		 sx[6] = "spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/TomcatDataSourceMetadata.java";
//		 sx[7] = "spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/AbstractDataSourceMetadata.java";
//		 sx[8] = "spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/CommonsDbcpDataSourceMetadata.java";
//		 sx[9] = "spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jdbc/DataSourceMetadata.java";
//		 
		 
		 Map<FileKey, ChangeType> modifiedfiles = new TreeMap<FileKey, ChangeType>();
		 
		 List<String> l1 = Arrays.asList(s1.split("/"));
		 
		 Set<String> setA = l1.stream().flatMap(s ->  Arrays.asList(s.split("(?=\\p{Lu})")).stream()).collect(Collectors.toSet());
		 
//		 Set<String> setA = new HashSet<String>(Arrays.asList(s1.split("(?=[-|.|>])"))); //Set.of(s1.split("/"));
		 
		 int lenA = setA.size();

		 for (String s : sx) {
			 Set<String> setB = Set.of(s.split("/"));
			 
			 Set<String> intersectSet = setA.stream()
					    .filter(setB::contains)
					    .collect(Collectors.toSet());
	 
			 double idx = (double) intersectSet.size() / lenA;
			 modifiedfiles.put(new FileKey(s, idx), ChangeType.MODIFY);
		 }
		 
		 System.out.println(s1);
//		 for( Entry<FileKey, ChangeType> e : modifiedfiles.entrySet()) {
//			 System.out.println(e.getKey().fileName +" - "+ e.getKey().matchIndex);
//		 }
		 
		 modifiedfiles.entrySet().forEach( e -> System.out.println(e.getKey().fileName +" - "+ e.getKey().matchIndex));
		 
		 Map<FileKey, ChangeType> collect = modifiedfiles.entrySet().stream()
		 .filter(x -> x.getKey().fileName.contains("/actuate"))
		 .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue(), (a,b) -> a,  TreeMap<FileKey, ChangeType>::new ));
		 
		 System.out.println("after filter");
		 collect.entrySet().forEach( e -> System.out.println(e.getKey().fileName +" - "+ e.getKey().matchIndex));
		 
	}

}
