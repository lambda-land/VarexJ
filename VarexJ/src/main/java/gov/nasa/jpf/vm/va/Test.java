package gov.nasa.jpf.vm.va;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gov.nasa.jpf.vm.va.IStackHandler.Type;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class Test {
	public static void main(String[] args) {
		VStackTest();
	    /*StackHandlerFactory.activateHybridStackHandler();
		IStackHandler stack = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 2);
		//IVStack stack = new ConditionalStack(2);

		FeatureExpr f1 = FeatureExprFactory.createDefinedExternal("f1" + System.currentTimeMillis());

		Conditional<Integer> n1 = One.valueOf((int) (Math.random() * 10 + 1));
		stack.push(FeatureExprFactory.True(), n1, true);
		assertEquals(1, stack.getStackWidth());
		Conditional<Integer> n2 = new One<>((int) (Math.random() * 1000));
		stack.push(f1, n2, false);
		stack.push(f1.not(), n2, false);
		if (!(stack instanceof BufferedStackHandler)) {
			System.out.println(stack);
			System.out.println("width " + stack.getStackWidth());
		}*/
	}
	public static void VStackTest() {
	    IVStack vstack = new VStack(10);
	    FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
	    FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
	    FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");
	    Conditional<Integer> v1 = ChoiceFactory.create(c, new One<>(3), new One<>(4));
	    Conditional<Integer> v2 = ChoiceFactory.create(a, new One<>(1), new One<>(2));
	   // vstack.push(b, new One<>(2), false);
	    vstack.push(FeatureExprFactory.True(), v1, false);
	    vstack.push(b.not(), v2, false);
	
	    System.out.println(vstack);
	    Conditional<Integer> res = vstack.pop(b, Type.INT);
	    System.out.println("res " + res);
	    System.out.println(vstack);
	}
}
