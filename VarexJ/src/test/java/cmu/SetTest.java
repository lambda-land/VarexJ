package cmu;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import org.junit.Test;

import gov.nasa.jpf.annotation.Conditional;
import gov.nasa.jpf.util.test.TestJPF;


public class SetTest extends TestJPF {

	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=interaction",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice"};
	
	@Test
	public void setTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				NonStaticFeature[] options = getOptions(2);
				
				HashSet<Integer> set = new HashSet<Integer>();
		
				
				for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						System.out.println(i);
						set.add(i);
					}
				}
				
				
				set.size();
/*
			    Iterator itr = set.iterator();

			    while (itr.hasNext()){
			      System.out.println(itr.next());
			    }
				//System.out.println("Size: " + set.size());
				//System.out.println(set.toString());
	
				int sum = 0;
				for (Integer element : set) {
					sum += element; 
				}
				if (sum == 10) {
					System.out.println(set);
				} 
					*/
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private static NonStaticFeature[] getOptions(int nrOptions) {
		NonStaticFeature[] options = new NonStaticFeature[nrOptions];
		for (int i = 0; i < options.length; i++) {
			options[i] = new NonStaticFeature();
		}
		return options;
	}

}