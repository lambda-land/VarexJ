package cmu;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import org.junit.Test;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.One;
import gov.nasa.jpf.annotation.Conditional;
import gov.nasa.jpf.util.test.TestJPF;



public class SetTest extends TestJPF {

	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=interaction",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice"};
	
  
	@Test
	public void setTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				System.out.println("******************setTest*********************");
				NonStaticFeature[] options = getOptions(5);
			    HashSet<Integer> set = new HashSet<Integer>();
			    HashSet<Integer> tset = new HashSet<Integer>();
			    for(int i= 0; i <5; i++){
			    	if(options[0].a){tset.add(0);}
			    	if(i!= 0){
			    	tset.add(i);
			    	}
			    }
			    //System.out.println(tset);
			   
			    for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						set.add(i);
					}
					
				}
				int sum = 0;
				for (Integer element : set) {
					sum += element; 
				}
				if (sum == 10) {
					assertTrue(tset.equals(set));
				}
				
			} catch (Exception e) {
				e.printStackTrace();	
			}
		}
	}
		
	@Test
	public void containsTest() throws Exception {	
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				System.out.println("******************containsTest*********************");
				NonStaticFeature[] options = getOptions(2);
			    HashSet<Integer> set = new HashSet<Integer>();
			    //Map(1->a,2->b)
			    if (options[0].a){set.add(1);}
			    if (options[1].a){set.add(2);}
	
			    System.out.println(set);
				if(options[0].a && options[1].a){
					assertTrue(set.contains(1) && set.contains(2) == true);
				}if(options[0].a && (!options[1].a)){
					assertTrue(set.contains(1) == true);
				}if((!options[0].a) && options[1].a){
					assertTrue(set.contains(2) == true);
				}else{
					assertTrue(set.contains(1) || set.contains(2) == false);
				}
				if(options[0].a){
					assertTrue(set.contains(0) == false);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
    }		
	
	@Test
	public void sizeTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				System.out.println("******************sizeTest*********************");
				NonStaticFeature[] options = getOptions(2);
			    HashSet<Integer> set = new HashSet<Integer>();
			    int tsize = 0;
			    for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						set.add(i);
						tsize++;
					}
				}
			    
				if(options[0].a && options[1].a){
					assertTrue(set.size() == 2);
				}else if(options[0].a || options[1].a){
					assertTrue(set.size() == 1);
				}else if(!(options[0].a && options[1].a)){
					assertTrue(set.size() == 0);
				}
				assertTrue(tsize == set.size());
				if(options[0].a){
					assertTrue(tsize == set.size());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	@Test
	public void removeTest() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				System.out.println("******************removeTest*********************");
				NonStaticFeature[] options = getOptions(3);
				NonStaticFeature[] options1 = getOptions(2);
			    HashSet<Integer> set = new HashSet<Integer>();
			    HashSet<Integer> tset = new HashSet<Integer>();
			    for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						set.add(i);
					}
				}
			    for (int i = 0; i < options1.length; i++) {
					if (options[i].a) {
						tset.add(i);
					}
				}
			    set.remove(2);
			    assertTrue(set.equals(tset));
			}catch (Exception e) {
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
