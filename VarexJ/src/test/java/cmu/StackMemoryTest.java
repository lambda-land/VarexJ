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




public class StackMemoryTest extends TestJPF {
	// try both hashset;
	static String[] JPF_CONFIGURATION = new String[]{/*"+interaction=interaction",*/ "+search.class= .search.RandomSearch", "+choice=MapChoice"};
	
	private void testN(int n) {
		//if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			try {
				NonStaticFeature[] options = getOptions(n);
				HashSet<Integer> set = new HashSet<Integer>();
				for (int i = 0; i < options.length; i++) {
					if (options[i].a) {
						set.add(i);
					}
				}
				for(int i = 0; i < options.length; i++){
					if(options[0].a){
						set.add(i);
					}
				}
				//set.contains(0);  
					
				int sum = 0;
				for (Integer element : set) {
					//System.out.println("");
					sum += element;
				} 
				set.contains(0);
			/*
			    for(int i= 0; i <= n; i++){
			    	set.remove(i);
			    }
				*/
					
			} catch (Exception e) {
				e.printStackTrace();	
			}
		//		int[] s = new int[100];
}
	
	@Test
	public void setTest() throws Exception {
		//Map<Integer, Long> res = new HashMap<Integer,Long>(200);
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
		PrintStream out = new PrintStream(new FileOutputStream("/home/meng/Documents/VStack.txt"),true);
		HashSet<Integer> set = new HashSet<Integer>();
		for(int i = 0; i < 50; ++i) set.add(i);
		for(int i = 29; i <= 29; ++i) {
			System.out.println("******************setTest*********************");
			long startTime = System.currentTimeMillis();
			
			//if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
				testN(i);
			//}
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("That took " + (totalTime) + " milliseconds in " + i + " features" );
			 // Get the Java runtime
		    Runtime runtime = Runtime.getRuntime();
		    // Run the garbage collector
		    runtime.gc();
		    // Calculate the used memory
		    long memory = runtime.totalMemory() - runtime.freeMemory();
		    System.out.println("Used memory is bytes: " + memory);
		    //System.out.println("Used memory is megabytes: "+ bytesToMegabytes(memory));
			out.print(totalTime + " ");
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
