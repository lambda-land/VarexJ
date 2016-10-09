package gov.nasa.jpf.vm.va;

import java.util.ArrayList;
import java.util.List;

import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.vm.StackFrame;

public class StackHandlerFactory {

	public enum SHFactory {
		Default, Hybid, Buffered //, VStack 
	}
	
	public static List<Object> asParameter() {
		List<Object> factorys = new ArrayList<>();
		for (Object f : SHFactory.values()) {
			factorys.add(f);
		}
		return factorys;
	}
	
	public static void setFactory(SHFactory factory) {
		switch (factory) {
		case Buffered:
			activateBufferedStackHandler();
			break;
		case Default:
			activateDefaultStackHandler();
			break;
		case Hybid:
			activateHybridStackHandler();
			break;
		/*
		case VStack:
			activateVStackHandler();
			break;
		*/
		default:
			throw new RuntimeException(factory + " not supported");
		}
	}
	
	//static Factory f = new DefaultStackHandlerFactory();
	static Factory f = new HybridStackHandlerFactory();
	//static Factory f = new VStackHandlerFactory();

	public static Factory getCurrent() {
		return f;
	}

	public static IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return createStack(ctx, nLocals, nOperands, null);
	}
	
	public static IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands, StackFrame frame) {
		//return f.createStack(ctx, nLocals, nOperands);
		//return new VStackHandler(ctx, nLocals, nOperands);
		return new StackHandler(ctx, nLocals, nOperands);
		//return new HybridStackHandler(ctx, nLocals, nOperands);
		//return new MeasuringStackHandler(ctx, nLocals, nOperands, frame);
	}
	
	public static IStackHandler createStack2(FeatureExpr ctx, int nLocals, int nOperands) {
		return f.createStack(ctx, nLocals, nOperands);
	}

	public static IStackHandler createStack() {
		return f.createStack();
	}

	public static void activateBufferedStackHandler() {
		f = new BufferedStackHandlerFactory();
	}

	public static void activateOneStackHandler() {
		f = new OneStackHandlerFactory();
	}
	
	public static void activateJPFStackHandler() {
		f = new JPFStackHandlerFactory();
	}

	public static void activateHybridStackHandler() {
		f = new HybridStackHandlerFactory();
	}
	/*
	public static void activateVStackHandler() {
		f = new VStackHandlerFactory();
	}*/

	public static void activateDefaultStackHandler() {
		f = new DefaultStackHandlerFactory();
	}
}

interface Factory {
	IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands);

	IStackHandler createStack();
}

class OneStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new OneStackHandler(ctx, nLocals, nOperands);
	}

	@Override
	public IStackHandler createStack() {
		return new OneStackHandler();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

class JPFStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new JPFStackHandler(ctx, nLocals, nOperands);
	}

	@Override
	public IStackHandler createStack() {
		return new JPFStackHandler();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

class DefaultStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new StackHandler(ctx, nLocals, nOperands);
	}

	@Override
	public IStackHandler createStack() {
		return new StackHandler();
	}

	@Override
	public String toString() {
		return StackHandler.class.getSimpleName();
	}
}

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

class HybridStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new HybridStackHandler(ctx, nLocals, nOperands);
	}

	@Override
	public IStackHandler createStack() {
		return new HybridStackHandler();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +HybridStackHandler.normalStack + " -> " + HybridStackHandler.liftedStack + ")";
	}
}
	/*
class VStackHandlerFactory implements Factory {
	@Override
	public IStackHandler createStack(FeatureExpr ctx, int nLocals, int nOperands) {
		return new VStackHandler(ctx, nLocals, nOperands);
	}

	@Override
	public IStackHandler createStack() {
		return new VStackHandler();
	}
	public String toString() {
		return VStackHandler.class.getSimpleName();
	}
	
}*/
