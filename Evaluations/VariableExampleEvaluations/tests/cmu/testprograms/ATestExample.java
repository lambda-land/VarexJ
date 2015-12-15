package cmu.testprograms;

import gov.nasa.jpf.util.test.TestJPF;

/**
 * Abstract class for example tests.<br>
 * Override {@link #getChoiceFactory()} to specify the used Choice implementation.
 * 
 * @author Jens Meinicke
 */
public abstract class ATestExample extends TestJPF {

	protected final String[] config = {/*"+invocation=true",*/ "+search.class= .search.RandomSearch", "+classpath=bin;" + getClassPath(), "+featuremodel=" + getModelFolder() + getModelFile(), "+choice=" + getChoiceFactory(), "+nhandler.delegateUnhandledNative"};
	
	protected String getChoiceFactory() {
		return "TreeChoice";
	}

	protected abstract String getClassPath();

	protected abstract String getModelFile();
	
	protected String getModelFolder() {
		return "models/";
	}
	
}
