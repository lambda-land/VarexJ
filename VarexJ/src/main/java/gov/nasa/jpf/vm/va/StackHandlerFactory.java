package gov.nasa.jpf.vm.va;

import de.fosd.typechef.featureexpr.FeatureExpr;

public class StackHandlerFactory {
	
	static Factory f = new DefaultStackHandlerFactory();

	public static IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return f.createStack(ctx, nLocals, nOperands);
	}
	
	public static IStackHandler createStack() {
		return f.createStack();
	}
/*
	public static void activateBufferedStackHandler() {
		f = new BufferedStackHandlerFactory();
	}
	*/
	public static void activateDefaultStackHandler() {
		f = new DefaultStackHandlerFactory();
	}
	
	public static IStackHandler createStack2(FeatureExpr ctx, int nLocals, int nOperands) {
		return f.createStack(ctx, nLocals, nOperands);
	}
}

interface Factory {
	IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands);
	IStackHandler createStack();
}

class DefaultStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new StackHandlerLog(ctx, nLocals, nOperands);
	}
	@Override
	public IStackHandler createStack() {
		return new StackHandlerLog();
	}
}
/*
class BufferedStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new BufferedStackHandler(ctx, nLocals, nOperands);
	}
	@Override
	public IStackHandler createStack() {
		return new BufferedStackHandler();
	}
}
*/
