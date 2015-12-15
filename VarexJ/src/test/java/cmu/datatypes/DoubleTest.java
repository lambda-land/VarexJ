package cmu.datatypes;

import org.junit.Test;

import gov.nasa.jpf.annotation.Conditional;
import gov.nasa.jpf.util.test.TestJPF;

@SuppressWarnings("unused")
public class DoubleTest extends TestJPF {

	static String JPF_CONFIGURATION = "+search.class= .search.RandomSearch";
	
	@Conditional
	static boolean a = true;
	@Conditional
	static boolean b = true;
	@Conditional
	static boolean c = true;
	@Conditional
	static boolean d = true;
	
	@Conditional
	static boolean x = true;
	@Conditional
	static boolean y = true;
	@Conditional
	static boolean z = true;

	
	@Test
	public void testFloat() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			double i = 13;
			double j = 36;
			if (z) {
				i = 2;
			}
			if (y) {
				j = 4;
			}
			
			double k = 0;
			k = i + j;
			k = i - j;
			k = i * j;
			k = i / j;
			k = i % j;
		}
	}

	@Test
	public void testDoubleReturn() throws Exception {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			double d = method();
		}
	}

	private double method() {
		if (c) {
			return 1;
		}
		double d = 1;
		return 0;
	}
}
