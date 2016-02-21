package cmu;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
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
				NonStaticFeature[] options = getOptions(3);
				
				HashSet<Integer> set = new HashSet<Integer>();
		
				for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						System.out.println(i);
						set.add(i);
					}
				}
				
				System.out.println("size: " + set.size());
			    System.out.println(set);
			    System.out.println("contains: " + set.contains(0));
			    System.out.println("contains: " + set.contains(1));
				System.out.println("*****************************************");
			    set.remove(1);
			    System.out.println(set);
				System.out.println("*****************************************");
			
				int sum = 0;
				for (Integer element : set) {
					sum += element; 
				}
				if (sum == 10) {
					System.out.println(set);
				}	 
		
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
