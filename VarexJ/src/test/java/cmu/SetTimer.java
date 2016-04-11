package cmu;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.*;
import java.util.*;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.One;
import gov.nasa.jpf.annotation.Conditional;
import gov.nasa.jpf.util.test.TestJPF;




public class SetTimer extends TestJPF {
	// try both hashset;
	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=interaction",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice"};
	
  
	@Test
	public void setTest() throws Exception {
		//Map<Integer, Long> res = new HashMap<Integer,Long>(200);
		PrintStream out = new PrintStream(new FileOutputStream("/home/meng/Documents/OutFile.txt"),true);
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				for(int n = 1; n <= 20; n++){
					long startTime = System.currentTimeMillis();
				    System.out.println("******************setTest*********************");
					NonStaticFeature[] options = getOptions(n);
				    HashSet<Integer> set = new HashSet<Integer>();
				
				    for (int i = 0; i < options.length; i++) {
						if (options[i].a) {
							set.add(i);
						}
					}
				    
				    /*
				    for(int i= 0; i <= n; i++){
				    	set.contains(i);
				    }
				    
				
				    int sum = 0;
					for (Integer element : set) {
						sum += element; 
					}*/
					
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					System.out.println("That took " + (totalTime) + " milliseconds in " + n + " features" );
					out.print(totalTime + " ");
					}
				
			
				} catch (Exception e) {
					e.printStackTrace();	
				}
		    out.close();
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
