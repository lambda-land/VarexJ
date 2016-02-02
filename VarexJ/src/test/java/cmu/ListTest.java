package cmu;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class ListTest extends TestJPF {

	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=stack",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice", 
//			"+stack=BufferedStackHandler"
		};
	

	
	@Test
	public void listTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			NonStaticFeature[] options = getOptions(3);
			
			List<Integer> list = new LinkedList<>();
			
			for (int i = 0; i < options.length; i++) {
				System.out.println(i);
				if (options[i].a) {
					list.add(i);
				}
			}
			for (Integer element : list) {
				System.out.println(element);
			}
			System.out.println(list);
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

