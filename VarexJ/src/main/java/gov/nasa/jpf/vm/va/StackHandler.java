 package gov.nasa.jpf.vm.va;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Types;


/**
 * Stack implementation where locals are separated from stack.<br>
 * Locals: Conditional[]<br>
 * Stack: Conditional -Stack-
 * 
 * TODO:
 * 1. tolist 
 * 2. popEntry
 * 3. return value Conditional<Boolan> or Boolean
 * 4. understand some functions 
 *
 */
public class StackHandler implements Cloneable, IStackHandler {

	/** Locals are directly accessed with index **/
	protected Conditional<Entry>[] locals;

	protected VStack stack;

	protected int length = 0;

	public FeatureExpr stackCTX;
	
	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#getStackWidth()
	 */
	@Override
	
	public int getStackWidth() {
		return stack.stackwidthHelper().keySet().size();
		//return stack.topSet(FeatureExprFactory.True()).toList().size();
		//return stack.toList().size();
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#toString()
	 */
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("Locals: [");

		for (int i = 0; i < locals.length; i++) {
			if (locals[i] == null) {
				string.append("null");
			} else {
				string.append(locals[i]);
			}
			string.append(" ");
		}
		string.append("] \nStack: ");
		string.append(stack);
		return string.toString();
	}
	
	protected static final One<Entry> nullValue = new One<>(new Entry(MJIEnv.NULL, false)); 

	@SuppressWarnings("unchecked")
	public StackHandler(FeatureExpr ctx, int nLocals, int nOperands) {
		if (ctx == null) {
			// if loading class inside jetty, the ctx is null
			System.err.println("CAUTIOUS! CTX == NULL, creating True");
			ctx = FeatureExprFactory.True();
//			throw new RuntimeException("CTX == NULL");
		}
		length = nLocals + nOperands;
		locals = new Conditional[nLocals];
		Arrays.fill(locals, nullValue);
		//stack = new One<>(new Stack(nOperands));
		stack = new VStack(nOperands);
		stackCTX = ctx;
	}

	@SuppressWarnings("unchecked")
	public StackHandler() {
		//stack = new One<>(new Stack(0));
		stack = new VStack(0);
		locals = new Conditional[0];
		stackCTX = FeatureExprFactory.True();
	}
	
	@Override
	public FeatureExpr getCtx() {
		return stackCTX;
	}
	
	@Override
	public void setCtx(FeatureExpr ctx) {
		stackCTX = ctx;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#clone()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public StackHandler clone() {
		StackHandler clone = new StackHandler();
//		clone.setCtx(stackCTX); // TODO ThreadStopTest.testStopRunning() fails
		clone.length = length;
		clone.locals = new Conditional[locals.length];
		for (int i = 0; i < locals.length; i++) {
			Conditional<Entry> local = locals[i];
			if (local != null) {
				clone.locals[i] = local.map(CopyEntry);
			}
		}

		//clone.stack = stack.map(CopyStack);
		clone.stack = stack.copy();
		return clone;
	}
	
	private static final Function<Entry, Entry> CopyEntry = new Function<Entry, Entry>() {
		@Override
		public Entry apply(final Entry entry) {
			return entry.copy();
		}
	};
	
	

	/*
	 * ############################################################
	 * Handling of local variables
	 * ############################################################
	 */

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#pushLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public void pushLocal(final FeatureExpr ctx, final int index) {
		Conditional<Entry> value = locals[index];
		if (value == null) {
			value = new One<>(new Entry(MJIEnv.NULL, false));
		}
		value.mapf(ctx, new VoidBiFunction<FeatureExpr, Entry>() {

			@Override
			public void apply(final FeatureExpr ctx, final Entry entry) {
				push(ctx, entry.value, entry.isRef);
			}
		});
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#pushLongLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public void pushLongLocal(FeatureExpr ctx, int index) {
		Conditional<Entry> value = locals[index];
		if (value == null) {
			value = new One<>(new Entry(0, false));
		}
		value.mapf(ctx, new VoidBiFunction<FeatureExpr, Entry>() {

			@Override
			public void apply(final FeatureExpr ctx, final Entry entry) {
				push(ctx, entry.value, false);
			}
		});
		value = locals[index + 1];
		if (value == null) {
			value = new One<>(new Entry(0, false));
		}
		value.mapf(ctx, new VoidBiFunction<FeatureExpr, Entry>() {

			@Override
			public void apply(final FeatureExpr ctx, final Entry entry) {
				push(ctx, entry.value, false);
			}
		});
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#storeOperand(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public void storeOperand(final FeatureExpr ctx, final int index) {
		if (Conditional.isTautology(ctx)) {
			locals[index] = popEntry(ctx, true);
		} else {
			if (locals[index] == null) {
				locals[index] = new One<>(new Entry(MJIEnv.NULL, false));
			}
			locals[index] = ChoiceFactory.create(ctx, popEntry(ctx, true), locals[index]).simplify();
		}
	}

	private Conditional<Entry> popEntry(FeatureExpr ctx, final boolean copyRef) {
		return stack.pop(ctx);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#storeLongOperand(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public void storeLongOperand(final FeatureExpr ctx, final int index) {
	
		locals[index + 1] = ChoiceFactory.create(ctx, popEntry(ctx, false), locals[index + 1]);
		locals[index] = ChoiceFactory.create(ctx, popEntry(ctx, false), locals[index]);
	
		locals[index] = locals[index].simplify();
		locals[index + 1] = locals[index + 1].simplify();
		
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#setLocal(de.fosd.typechef.featureexpr.FeatureExpr, int, cmu.conditional.Conditional, boolean)
	 */
	@Override
	public void setLocal(FeatureExpr ctx, final int index, final Conditional<Integer> value, final boolean isRef) {
		value.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

			@Override
			public void apply(final FeatureExpr x, final Integer value) {
				setLocal(x, index, value, isRef);
			}

		});
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#setLocal(de.fosd.typechef.featureexpr.FeatureExpr, int, int, boolean)
	 */
	@Override
	public void setLocal(final FeatureExpr ctx, final int index, final int value, final boolean isRef) {
		if (Conditional.isTautology(ctx)) {
			locals[index] = new One<>(new Entry(value, isRef));
		} else {
			if (locals[index] == null) {
				locals[index] = new One<>(new Entry(0, false));
			}
			locals[index] = ChoiceFactory.create(ctx, new One<>(new Entry(value, isRef)), locals[index]).simplify();
		}
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#getLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public Conditional<Integer> getLocal(FeatureExpr ctx, final int index) {
		if (index < 0) {
			return new One<>(-1);
		}
		if (index < locals.length) {
			if (locals[index] == null) {
				return One.MJIEnvNULL;
			}
			return locals[index].simplify(ctx).map(GetLocal).simplifyValues();
		} else {
			final int i = index - locals.length;
			return stack.getInteger(ctx, i);
		}
	}

	@Override
	public Object getLocal(int index) {
		return locals[index].simplify(getCtx());
	}
	
	private static final Function<Entry, Integer> GetLocal = new Function<Entry, Integer>() {
		@Override
		public Integer apply(final Entry x) {
			if (x == null) {
				return MJIEnv.NULL;
			}
			return x.value;
		}
	};

	// TODO change to conditional
	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#isRefLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public boolean isRefLocal(FeatureExpr ctx, final int index) {
		//System.out.println("stackhandler+++++++++++++++++++++" + this);
		if (index < 0) {
			return false;
		}

		if (index < locals.length) {
			if (locals[index] == null) {
				return false;
			}
			// TODO check calls of isRefLocal
			for (boolean b : locals[index].simplify(ctx).map(IsRefLocal).toList()) {
				if (b) {
					return true;
				}
			}
			return false;
//			return locals[index].simplify(ctx).map(new IsRefLocal()).simplifyValues().getValue();
		} else {
			final int i = index - locals.length;
			return stack.isRefIndex(ctx, i).simplifyValues().getValue();
			
		}
	}
	
	private static final Function<Entry, Boolean> IsRefLocal = new Function<Entry, Boolean>() {
		@Override
		public Boolean apply(final Entry y) {
			return y.isRef;
		}
	};

	/*
	 * #######################################################
	 * Handling of the stack
	 * ########################################################
	 */

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#push(de.fosd.typechef.featureexpr.FeatureExpr, T)
	 */
	@Override
	public <T> void push(final FeatureExpr ctx, final T value) {
		push(ctx, value, false);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#push(de.fosd.typechef.featureexpr.FeatureExpr, java.lang.Object, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void push(final FeatureExpr ctx, final Object value, final boolean isRef) {
		stack.push(ctx, value, isRef);
	}


	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#pop(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public Conditional<Integer> pop(final FeatureExpr ctx) {
		return pop(ctx, Type.INT);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#popLong(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public Conditional<Long> popLong(final FeatureExpr ctx) {
		return pop(ctx, Type.LONG);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#popDouble(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public Conditional<Double> popDouble(final FeatureExpr ctx) {
		return pop(ctx, Type.DOUBLE);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#popFloat(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public Conditional<Float> popFloat(final FeatureExpr ctx) {
		return pop(ctx, Type.FLOAT);
	}
	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#pop(de.fosd.typechef.featureexpr.FeatureExpr, gov.nasa.jpf.vm.StackHandler.Type)
	 */
	@Override
	public <T> Conditional<T> pop(final FeatureExpr ctx, final Type t) {
		switch (t) {
		case INT:
			return stack.popInteger(ctx).map(new Function<Integer, T>(){
				public T apply(Integer x) {
					return (T) x;
				}
			}).simplify().simplifyValues();
			//res = Integer.valueOf(lo);

			
		case DOUBLE:
			final Conditional<Integer> tmp = stack.popInteger(ctx);
			final Conditional<Integer> tmp1 = stack.popInteger(ctx);
			Conditional<T> res = tmp.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
				public Conditional<T> apply(FeatureExpr c, final Integer x) {
					return tmp1.simplify(c).map(new Function<Integer, T>(){
						public T apply(Integer y){
							return (T) (Double) Types.intsToDouble(x, y);
						}
					}).simplify();
				}
			}).simplify();
			return res;
			//res = Types.intsToDouble(lo, clone.pop());

		case FLOAT:
			Conditional<Float> res1 = stack.popInteger(ctx).map(new Function<Integer, Float>(){
				public Float apply(Integer x) {
					return  Types.intToFloat(x);
				}
			}).simplify().simplifyValues();
			return (Conditional<T>)(res1);
			//res = Types.intToFloat(lo);
		case LONG:
			final Conditional<Integer> reslong = stack.popInteger(ctx);
			final Conditional<Integer> reslong1 = stack.popInteger(ctx);
			Conditional<T> res2 = reslong.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
				public Conditional<T> apply(FeatureExpr c, final Integer x) {
					return reslong1.simplify(c).map(new Function<Integer, T>(){
						public T apply(Integer y){
							return (T) (Long) Types.intsToLong(x, y);
						}
					}).simplify();
				}
			}).simplify();
			return res2;
			//res = Types.intsToLong(lo, clone.pop());
			//break;
		default:
			return null;
		}
		//return (Conditional<T>)stack.popInteger(ctx);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#pop(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public void pop(FeatureExpr ctx, final int n) {
		for(int i = 0; i < n; i++){
			 stack.pop(ctx);
		}
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#peek(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public Conditional<Integer> peek(FeatureExpr ctx) {
		return peek(ctx, 0);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#peek(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public Conditional<Integer> peek(FeatureExpr ctx, final int offset) {
		return peek(ctx, offset, Type.INT);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#peekDouble(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public Conditional<Double> peekDouble(FeatureExpr ctx, final int offset) {
		return peek(ctx, offset, Type.DOUBLE);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#peekLong(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public Conditional<Long> peekLong(FeatureExpr ctx, final int offset) {
		return peek(ctx, offset, Type.LONG);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#peekFloat(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public Conditional<Float> peekFloat(FeatureExpr ctx, final int offset) {
		return peek(ctx, offset, Type.FLOAT);
	}

	protected <T> Conditional<T> peek(FeatureExpr ctx, final int offset, final Type t) {
		switch (t) {
		case DOUBLE:
			final Conditional<Integer> tmp = stack.peekValue(ctx, offset);
			final Conditional<Integer> tmp1 = stack.peekValue(ctx, offset+1);
			Conditional<T> res = tmp.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
				public Conditional<T> apply(FeatureExpr c, final Integer x) {
					return tmp1.simplify(c).map(new Function<Integer, T>(){
						public T apply(Integer y){
							return (T) (Double) Types.intsToDouble(x, y);
						}
					}).simplify();
				}
			}).simplify();
			return res;
			//stack.peekValue(ctx, offset).
			//return (T) (Double) Types.intsToDouble(stack.peekValue(ctx, offset).getValue(), stack.peekValue(ctx, offset + 1).getValue());
		case FLOAT:
			return stack.peekValue(ctx, offset).map(new Function<Integer, T>(){
				public T apply(Integer x) {
					return (T) (Float) Types.intToFloat(x);
				}
			}).simplify().simplifyValues();
			//return (T) (Float) Types.intToFloat(stack.peek(offset));
		case INT:
			return stack.peekValue(ctx, offset).map(new Function<Integer, T>(){
				public T apply(Integer x) {
					return (T) (Integer) x;
				}
			}).simplify().simplifyValues();
		case LONG:
			final Conditional<Integer> tmp2 = stack.peekValue(ctx, offset);
			final Conditional<Integer> tmp3 = stack.peekValue(ctx, offset+1);
			Conditional<T> res2 = tmp2.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
				public Conditional<T> apply(FeatureExpr c, final Integer x) {
					return tmp3.simplify(c).map(new Function<Integer, T>(){
						public T apply(Integer y){
							return (T) (Long) Types.intsToLong(x, y);
						}
					}).simplify();
				}
			}).simplify();
			return res2;
			
			//return (T) (Long) Types.intsToLong(stack.peek(offset), stack.peek(offset + 1));
			
		default:
			return null;
		}
		//return (Conditional<T> )stack.peek(ctx, offset);  
		 
		
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#isRef(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public boolean isRef(final FeatureExpr ctx, final int offset) {// change to Conditional<Boolean>
		return stack.isRef(ctx, offset).getValue();
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#set(de.fosd.typechef.featureexpr.FeatureExpr, int, int, boolean)
	 */
	@Override
	public void set(final FeatureExpr ctx, final int offset, final int value, final boolean isRef) {
		stack.set(ctx, offset, value, isRef);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#getTop()
	 */
	@Override
	public Conditional<Integer> getTop() {
		return stack.topSet(FeatureExprFactory.True());
	}


	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#setTop(de.fosd.typechef.featureexpr.FeatureExpr, int)
	 */
	@Override
	public void setTop(final FeatureExpr ctx, final int i) {
		throw new RuntimeException();
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#clear(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void clear(final FeatureExpr ctx) {
		stack.clear(ctx);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#getSlots()
	 */
	@Override
	public int[] getSlots() {
		return getSlots(FeatureExprFactory.True());
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#getSlots(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public int[] getSlots(FeatureExpr ctx) {
		throw new RuntimeException();
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof StackHandler)) {
			return false;
		}
		StackHandler other = (StackHandler) o;
		for (int i = 0; i < locals.length; i++) {
			Conditional<Entry> l1 = locals[i];
			Conditional<Entry> l2 = other.locals[i];
			if (l1 == l2) {
				continue;
			}
			if (l1 == null) {
				return false;
			}
			if (!l1.equals(l2)) {
				return false;
			}
		}
		if (!other.stack.equals(stack)) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#hashCode()
	 */
	@Override
	public int hashCode() {
		return 42;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#hasAnyRef(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	// return value?
	
	public boolean hasAnyRef(FeatureExpr ctx) {
		for (Conditional<Entry> local : locals) {
			if (local == null) {
				continue;
			}
			for (Entry entry : local.simplify(ctx).toList()) {
				if (entry.isRef) {
					return true;
				}
			}
		}
		//System.out.println("hasAnyRef +++++++++++++++++++++++++++++++++++++++++" + stack.hasAnyRef(ctx).simplify());
		return stack.hasAnyRef(ctx).simplify().getValue();
	}

	/*
	 * Stack Instructions
	 */

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#dup_x1(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void dup_x1(final FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP_X1);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#dup2_x2(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void dup2_x2(final FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP2_X2);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#dup2_x1(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void dup2_x1(final FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP2_X1);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#dup2(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void dup2(final FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP2);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#dup(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void dup(final FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#dup_x2(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void dup_x2(final FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP_X2);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpf.vm.IStackHandler#swap(de.fosd.typechef.featureexpr.FeatureExpr)
	 */
	@Override
	public void swap(final FeatureExpr ctx) {
		function(ctx, StackInstruction.SWAP);
	}

	void function(final FeatureExpr ctx, final StackInstruction instruction) {
		switch (instruction) {
			case DUP_X1:
				stack.dup_x1(ctx);
				break;
			case DUP2_X2:
				stack.dup2_x2(ctx);
				break;
			case DUP:
				stack.dup(ctx);
				break;
			case DUP2:
				stack.dup2(ctx);
				break;
			case DUP2_X1:
				stack.dup2_x1(ctx);
				break;
			case DUP_X2:
				stack.dup_x2(ctx);
				break;
			case SWAP:
				stack.swap(ctx);
				break;
			default:
				throw new RuntimeException(instruction + "not supported");
			}
	}

	@Override
	public int getLength() {
		return length;
	}
/*
	@Override
	public Conditional<Stack> getStack() {
		throw new RuntimeException();
	}
*/
	@Override
	public Set<Integer> getAllReferences() {
		Set<Integer> references = new HashSet<>();
		for (Conditional<Entry> cl : locals) {
			for (Entry l: cl.toList()) {
				if (l.isRef) {
					references.add(l.value);
				}
			}
		}
		
		references.addAll(stack.getReferences());
		
		
		return references;
	}

	@Override
	public int getLocalWidth() {
		int width = -locals.length;
		for (Conditional<Entry> local : locals) {
			width += local.simplify(getCtx()).toMap().size();
		}
		return width;
	}
	
	@Override
	public String getMaxLocal() {
		StringBuilder builder = new StringBuilder();
		for (Conditional<Entry> local : locals) {		
			int size = local.simplify(getCtx()).toMap().size();
			builder.append(local.simplify(getCtx()));
			builder.append(":");
			builder.append(size);
			builder.append('\n');
		}
		return builder.toString();
	}

	@Override
	public void IINC(FeatureExpr ctx, int index, final int increment) {
		locals[index] = locals[index].mapf(ctx, new BiFunction<FeatureExpr, Entry, Conditional<Entry>>() {

			@Override
			public Conditional<Entry> apply(FeatureExpr ctx, Entry y) {
				if (Conditional.isContradiction(ctx)) {
					return new One<>(y);
				}
				Entry copy = new Entry(y.value + increment, y.isRef);
				if (Conditional.isTautology(ctx)) {
					return new One<>(copy);
				}
				return ChoiceFactory.create(ctx, new One<>(copy), new One<>(y));
			}
			
			
		}).simplify();
	}

	

}
