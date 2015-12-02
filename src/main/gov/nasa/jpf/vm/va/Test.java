

package gov.nasa.jpf.vm.va;

import java.io.IOException;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class Test {
   
    public static void main(String[] args) throws IOException {
    
    	
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
		
		/*
		 *  E : Choice("A", 3, 2)
		 *  G : Choice("C", 1, 4)
		 *  F : CHoice("B", G, 10)
		 */
		Conditional<Integer> E = ChoiceFactory.create(ta, C, B);
		Conditional<Integer> G = ChoiceFactory.create(tc, A, D);
		Conditional<Integer> F = ChoiceFactory.create(tb, G, new One<>(10));
		
		
		stack.push(FeatureExprFactory.True(), B, false);
		stack.push(FeatureExprFactory.True(), C, true);
		stack.push(FeatureExprFactory.True(), D, false);
		stack.pop(FeatureExprFactory.True());
		stack.push(FeatureExprFactory.True(), E, false);
		stack.push(ta.not(), D, false);
		stack.dup(ta.not());
		stack.dup(ta.not());
		stack.pop(ta.not());
		stack.pop(ta, 2);
		
	//	System.out.println(StackHandler.q.toString());
		stack.push(tb, D);
		
		
		
//		System.out.println(StackHandler.q.toString());
//		System.out.println(stack.toString());
		
		
//		IStackHandler stack2 = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 10);
//		stack2.push(FeatureExprFactory.True(), A, true);
//		stack2.push(FeatureExprFactory.True(), B, false);
//		stack2.push(FeatureExprFactory.True(), C, true);
//		stack2.push(FeatureExprFactory.True(), D, false);
		//stack.push(FeatureExprFactory.True(), E, false);
		//stack2.push(FeatureExprFactory.True(), F, false);
		
		//System.out.println(stack);
        //System.out.println(stack.getTop());
		//System.out.println(stack.numOP().toString());
		//System.out.println(stack2.getTop());
		//System.out.println(stack2.numOP().toString());
		
    
    }

}