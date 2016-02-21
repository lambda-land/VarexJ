package cmu;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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

	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=interaction",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice"};
	

	@Test
	public void listTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				NonStaticFeature[] options = getOptions(5);
				
				List<Integer> list = new LinkedList<>();
				System.out.println("Create list");
				for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						System.out.println("i is "+ i);
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


