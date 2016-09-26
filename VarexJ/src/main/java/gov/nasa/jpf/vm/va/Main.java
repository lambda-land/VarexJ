package gov.nasa.jpf.vm.va;
import gov.nasa.jpf.jvm.JVMStackFrame;
import gov.nasa.jpf.vm.*;
import cmu.conditional.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;


class NonStaticFeature {
	FeatureExpr a = FeatureExprFactory.createDefinedExternal("f" + Main.FeatureID++);
	//boolean a = true;
	
}

public class Main {
	public static int FeatureID = 0;

	private static NonStaticFeature[] getOptions(int nrOptions) {
		NonStaticFeature[] options = new NonStaticFeature[nrOptions];
		for (int i = 0; i < options.length; i++) {
			options[i] = new NonStaticFeature();
		}
		return options;
	}
	
	public static FeatureExpr randomFEGen(NonStaticFeature[] options) {
		int i = (int)(Math.random() * options.length);
		FeatureExpr f = options[i].a;
		if(Math.random() < 0.5) {
			return f;
		} else {
			if(Math.random() < 0.5) return f.and(randomFEGen(options));
			else return f.or(randomFEGen(options));
		}
	}
	
	public static Conditional<Integer> randomCIGen(NonStaticFeature[] options, int sz) {		
		if(sz == 0) return One.valueOf((int)(Math.random() * Integer.MAX_VALUE));
		else {
			return ChoiceFactory.create(randomFEGen(options), randomCIGen(options, sz - 1), randomCIGen(options, sz - 1));
		}
	}
	
	public static Conditional<Integer> ratioGen(NonStaticFeature[] options, double ratio, int sz) {
		if(Math.random() < ratio) {
			return randomCIGen(options, sz);
		} else {
			return randomCIGen(options, 0);
		}
	}
	
	public static void activateTreeVStack() {
		ChoiceFactory.activateTreeChoice();
		StackFactory.activateVStack();
	}
	
	public static void activateTreeCStack() {
		ChoiceFactory.activateTreeChoice();
		StackFactory.activateCStack();
	}
	
	public static void testRatio(NonStaticFeature[] options, double ratio, int sz, int len) {
		StackFrame stack = new JVMStackFrame(0,20000);
		
		for(int i = 0; i < len; ++i) {
			FeatureExpr fe = FeatureExprFactory.True();
			Conditional<Integer> c = ratioGen(options, ratio, sz);
			System.out.println("" + i + " push " + fe + " " + c);
			stack.push(fe, c);
		}
		
		for(int i = 0; i < len; ++i) {
			FeatureExpr fe = randomFEGen(options);
			if(Math.random() < 0.5) {
				Conditional<Integer> c = ratioGen(options, ratio, sz);
				System.out.println("" + i + " push " + fe + " " + c);
				stack.push(fe, c);
			} else {
				Conditional<Integer> c = stack.pop(fe);
				System.out.println("" + i + " pop " + fe + " " + c);
			}
		}
	}
	
	public static void main(String[] args) {
		activateTreeCStack();
		NonStaticFeature[] options = getOptions(5);
		testRatio(options, 0.5, 1, 2);
        Store.print();

	}
	
	public static void test(String[] args) {
		//System.out.println("aaa");
		int n = 5, m = 5;
		//ChoiceFactory.activateMapChoice();
		ChoiceFactory.activateTreeChoice();
		//StackFactory.activateVStack();
		StackFactory.activateCStack();
		StackHandlerFactory.activateHybridStackHandler();
		StackFrame stack = new JVMStackFrame(0,20000);
		//StackHandlerFactory.activateDefaultStackHandler();
		//IStackHandler stack = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 10);
		NonStaticFeature[] options = getOptions(n);

		for(int i = 0; i < m; i++) {
			stack.push(FeatureExprFactory.True(), (i+1)*10, false);
		}
		
		for (int i = 0; i < options.length; i++) {
			System.out.println(options[i].a);
			//stack.push(options[i].a, (int)Math.random()*10000, false);
			//stack.push(options[i].a.not(), (int)Math.random()*10000, false);
			stack.push(options[i].a, (i+1), false);
			stack.push(options[i].a.not(), -(i+1), false);
		}
		
		for(int i = 0; i < m; i++) {
			stack.push(FeatureExprFactory.True(), (i+1)*10, false);
		}
        //stack.pop(options[0].a);
		for (int i = 0; i < options.length; i++) {
			System.out.println(options[i].a);
			System.out.println(stack.pop(options[i].a).simplify());
		}
        Store.print();
	}

}


