package cmu.defect4j.jfree.chart.annotations.junit;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import junit.framework.TestCase;
public class XYImageAnnotationTests extends TestJPF {

    private final String[] config = {"+nhandler.delegateUnhandledNative", "+classpath+=${jpf-core}/lib/junit-4.11.jar,${jpf-core}/lib/jfreechart-1.2.0-ea1.jar"};

    public static void main(String[] testMethods){
        runTestsOfThisClass(testMethods);
    }
	@Test(timeout=120000)
	public void testEquals() throws Exception {
		if (verifyNoPropertyViolation(config)) {
			TestCase testcase = new org.jfree.chart.annotations.junit.XYImageAnnotationTests("testEquals");
			testcase.run();
		}
	}

	@Test(timeout=120000)
	public void testCloning() throws Exception {
		if (verifyNoPropertyViolation(config)) {
			TestCase testcase = new org.jfree.chart.annotations.junit.XYImageAnnotationTests("testCloning");
			testcase.run();
		}
	}

	@Test(timeout=120000)
	public void testPublicCloneable() throws Exception {
		if (verifyNoPropertyViolation(config)) {
			TestCase testcase = new org.jfree.chart.annotations.junit.XYImageAnnotationTests("testPublicCloneable");
			testcase.run();
		}
	}

	@Test(timeout=120000)
	public void testHashCode() throws Exception {
		if (verifyNoPropertyViolation(config)) {
			TestCase testcase = new org.jfree.chart.annotations.junit.XYImageAnnotationTests("testHashCode");
			testcase.run();
		}
	}

}