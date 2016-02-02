package cmu;



import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Test;

import gov.nasa.jpf.annotation.Conditional;
import gov.nasa.jpf.util.test.TestJPF;

/**
 * 
 * Tests non stetic features.
 * 
 * @author Jens Meinicke
 *
 */
public class NonStaticVariabilityAwareTest extends TestJPF {

	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=stack",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice", 
//			"+stack=BufferedStackHandler"
		};
	
	@Test
	public void stackTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			System.out.println("verifyNoPropertyViolation(JPF_CONFIGURATION) is True");
			NonStaticFeature[] options = getOptions(10);
			
			double sum = 0;
			for (int i = 0; i < options.length; i++) {
				
				if (options[i].a) {
					System.out.println( i + " is true");
					//System.out.println(i);
					sum += 20.3d * i;
				}else{
					System.out.println( i + " is false");
				}
				
			}
			int r = new Random().nextInt(10)+100;
			System.out.println("r is " + r);
			System.out.println("sum is "+ sum );
		}
		
	}

	@Test
	public void listTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				NonStaticFeature[] options = getOptions(10);
				
				List<Integer> list = new LinkedList<>();
				System.out.println("Create list");
				for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						System.out.println(i);
						list.add(i);
					}
				}
				
				System.out.println("Size: " + list.size());
				for (int i = 0; i < list.size(); i++) {
					System.out.println(list.get(i));
				}
				
				int sum = 0;
				for (Integer element : list) {
					sum += element; 
				}
				if (sum == 10) {
					System.out.println(list);
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

class NonStaticFeature {

	@Conditional
	boolean a = true;
	
}
