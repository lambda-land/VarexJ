

package gov.nasa.jpf.vm.va;

import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.MapChoice;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;


public class Test {

    public static void main(String[] args) {
    	
    	
        FeatureExpr ta, tb, tc;
    	
    	IStackHandler stack = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 10);
		FeatureExpr ctx = FeatureExprFactory.True();
		Conditional<Integer> A = new One<>(1);
		Conditional<Integer> B = new One<>(2);
		Conditional<Integer> C = new One<>(3);
		Conditional<Integer> D = new One<>(4);
		
		ta = FeatureExprFactory.createDefinedExternal("A");
		tb = FeatureExprFactory.createDefinedExternal("B");
		tc = FeatureExprFactory.createDefinedExternal("C");
		Conditional<Integer> E = ChoiceFactory.create(ta, C, B);
		Conditional<Integer> G = ChoiceFactory.create(tc, A, D);
		Conditional<Integer> F = ChoiceFactory.create(tb, G, new One<>(10));
		
		
		//stack.push(FeatureExprFactory.True(), A, true);
		stack.push(FeatureExprFactory.True(), B, false);
		stack.push(FeatureExprFactory.True(), C, true);
		stack.push(FeatureExprFactory.True(), D, false);
		stack.push(FeatureExprFactory.True(), E, false);
		//stack.push(FeatureExprFactory.True(), F, false);
		//stack.push(FeatureExprFactory.True(), G, false);
        //stack.dup2_x2(ctx);
		stack.push(ta, B, false);
		
		IStackHandler stack2 = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 10);
		stack2.push(FeatureExprFactory.True(), A, true);
		stack2.push(FeatureExprFactory.True(), B, false);
		stack2.push(FeatureExprFactory.True(), C, true);
		stack2.push(FeatureExprFactory.True(), D, false);
		//stack.push(FeatureExprFactory.True(), E, false);
		//stack2.push(FeatureExprFactory.True(), F, false);
		
		
		
        System.out.println(stack.getTop());
		System.out.println(stack.numOP().toString());
		System.out.println(StackHandler.q.toString());
	
		
		System.out.println(stack2.getTop());
		System.out.println(stack2.numOP().toString());
		
    
    }

}