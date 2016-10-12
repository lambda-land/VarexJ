package gov.nasa.jpf.vm.va;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.va.IStackHandler.Type;



public class BufferedStack implements IVStack {
    IVStack stack;
 	private LinkedList<Tuple> buffer = new LinkedList<>();
 	private FeatureExpr bufferCTX = FeatureExprFactory.True();
 	private int maxStackSize;
 	public FeatureExpr stackCTX;

	@Override
	public void init(Stack st) {
		throw new RuntimeException();

	}
	public BufferedStack(int nOperands ) {
	
		maxStackSize = nOperands;
		stack = new ConditionalStack(nOperands);
		stack.setCtx(FeatureExprFactory.True());
	}
	
	@Override
	public int getStackWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FeatureExpr getCtx() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCtx(FeatureExpr ctx) {
		stack.setCtx(ctx);

	}

	@Override
	public boolean hasAnyRef(FeatureExpr ctx) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRefLocal(FeatureExpr ctx, int index) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Conditional<Integer> getTop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getSlots(FeatureExpr ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Integer> getAllReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Conditional<Integer> getInteger(FeatureExpr ctx, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}
	
	
	public void debufferAll() {
		//System.out.println("debufferAll()");
		//System.out.println(bufferCTX);
		final FeatureExpr ctx = bufferCTX;
		bufferCTX = FeatureExprFactory.False();
		while (!buffer.isEmpty()) {
			final Tuple value = buffer.removeLast();
			value.value.mapf(ctx, new VoidBiFunction<FeatureExpr, Object>() {

				@Override
				public void apply(final FeatureExpr ctx, final Object v) {
					stack.push(ctx, v, value.isRef);
				}

			});
		}
		bufferCTX = FeatureExprFactory.True();
	}
	
	void addToBuffer(Conditional<?> value, boolean isRef) {
		buffer.push(new Tuple(value, isRef));
	}
	
	class Tuple {
		
		final Conditional value;
		final boolean isRef;

		private Tuple(Conditional value, boolean isRef) {
			if (value instanceof Conditional) {
				this.value = value;
			} else {
				this.value = new One<>(value);
			}
			this.isRef = isRef;
		}

		@Override
		public String toString() {
			return "Tuple [value=" + value + ", isRef=" + isRef + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isRef ? 1231 : 1237);
			return prime * result + ((value == null) ? 0 : value.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tuple other = (Tuple) obj;
			if (isRef != other.isRef)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
	@Override
	public void push(FeatureExpr ctx, Object value, boolean isRef) {
		if (!(value instanceof Conditional)) {
			push(ctx, new One<>(value), isRef);
			return;
		}
		if (buffer.isEmpty()) {
			bufferCTX = ctx;
			addToBuffer(((Conditional) value).simplify(ctx), isRef);
		} else if (ctx.equivalentTo(bufferCTX)) {
			addToBuffer(((Conditional) value).simplify(ctx), isRef);
		} else {
			debufferAll();
			bufferCTX = ctx;
			addToBuffer(((Conditional) value).simplify(ctx), isRef);
		}
			
	}
    
	
	@Override
	public <T> Conditional<T> pop(FeatureExpr ctx, Type t) {
		if (!buffer.isEmpty()) {
			if (bufferCTX.equivalentTo(ctx)) {
				final Object value = buffer.peek().value.getValue(true);
				switch (t) {
				case DOUBLE:
					if (value instanceof Double) {
						return buffer.pop().value;
					}
					if (value instanceof Long) {
						return buffer.pop().value.map(new Function<Long, Double>() {

							@Override
							public Double apply(Long x) {
								return Types.longToDouble(x);
							}
							
						});
					}
					if (value instanceof Integer) {
						if (buffer.size() >= 2) {
							final Object value2 = buffer.get(1).value.getValue(true);
							if (value2 instanceof Integer) {
								final Conditional pop1 = buffer.pop().value;
								final Conditional pop2 = buffer.pop().value;
								return pop1.mapr(new Function<Integer, Conditional<Double>>() {
	
									@Override
									public Conditional<Double> apply(final Integer x1) {
										return pop2.map(new Function<Integer, Double>() {
	
											@Override
											public Double apply(Integer x2) {
												return Types.intsToDouble(x1, x2);
											}
											
										});
									}
									
								}).simplify();
							}
						}
						break;
					}
					throw new RuntimeException("Type " + value.getClass() + " not supported " + t);
				case FLOAT:
					if (value instanceof Float) {
						return buffer.pop().value;
					}
					if (value instanceof Integer) {
						return  buffer.pop().value.map(new Function<Integer, Float>() {

							@Override
							public Float apply(Integer x) {
								return Types.intToFloat(x);
							}
							
						});
					}
					throw new RuntimeException("Type " + value.getClass() + " not supported " + t);
				case INT:
					if (value instanceof Integer) {
						return buffer.pop().value;
					}
					if (value instanceof Byte ||
						value instanceof Short) {
						return  buffer.pop().value.map(new Function<Number, Integer>() {

							@Override
							public Integer apply(Number x) {
								return x.intValue();
							}
							
						});
					}
					if (value instanceof Float) {
							return  buffer.pop().value.map(new Function<Float, Integer>() {

								@Override
								public Integer apply(Float x) {
									return Types.floatToInt(x);
								}
								
							});
						}

					throw new RuntimeException("Type " + value.getClass() + " not supported " + t);
				case LONG:
					if (value instanceof Long) {
						return buffer.pop().value;
					}
					if (value instanceof Double) {
						return buffer.pop().value.map(new Function<Double, Long>() {

							@Override
							public Long apply(Double x) {
								return Types.doubleToLong(x);
							}
							
						});
					}
					if (value instanceof Integer) {
						if (buffer.size() >= 2) {
							final Object value2 = buffer.get(1).value.getValue(true);
							if (value2 instanceof Integer) {
								final Conditional pop1 = buffer.pop().value;
								final Conditional pop2 = buffer.pop().value;
								return pop1.mapr(new Function<Integer, Conditional<Long>>() {
	
									@Override
									public Conditional<Long> apply(final Integer x1) {
										return pop2.map(new Function<Integer, Long>() {
	
											@Override
											public Long apply(Integer x2) {
												return Types.intsToLong(x1, x2);
											}
											
										});
									}
									
								}).simplify();
							}
						}
						break;
					}
					throw new RuntimeException("Type " + value.getClass() + " not supported " + t);
				default:
					throw new RuntimeException("Type " + value.getClass() + " not supported " + t);
				}
				
				debufferAll();
			} else {
				debufferAll();
			}
		}
		return stack.pop(ctx, t);
	}

	@Override
	public void pop(FeatureExpr ctx, int n) {
		if (n == 0) {
			return;
		}
		if (!buffer.isEmpty()) {
			if (buffer.size() >= n && bufferCTX.equivalentTo(ctx)) {
				while (n > 0) {
					Object value = buffer.peek().value.getValue(true);
					if (value instanceof Integer || 
						value instanceof Float || 
						value instanceof Byte || 
						value instanceof Short) {
						buffer.removeFirst();
						n--;
						continue;
					}
					if (value instanceof Double || value instanceof Long) {
						if (n > 1) {
							buffer.removeFirst();
							buffer.removeFirst();
							n = n - 2;
							continue;
						} else {
							debufferAll();
							stack.pop(ctx, n);
							return;
						}

					}
					throw new RuntimeException("type " + value.getClass() + " missed");
				}
			} else {
				debufferAll();
				stack.pop(ctx, n);
			}
		} else {
			stack.pop(ctx, n);
		}
	}

	@Override
	public void setRef(FeatureExpr ctx, int index, boolean ref) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Conditional<T> peek(FeatureExpr ctx, int offset, Type t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Conditional<Entry> popEntry(FeatureExpr ctx, boolean copyRef) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public <T> void remove(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRef(FeatureExpr ctx, int offset) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(FeatureExpr ctx, int offset, int value, boolean isRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTop(FeatureExpr ctx, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public IVStack clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Conditional<Stack> getStack() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dup_x1(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dup2_x2(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dup2_x1(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dup2(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dup(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dup_x2(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void swap(FeatureExpr ctx) {
		// TODO Auto-generated method stub

	}
  @Override
  public void pushEntry(FeatureExpr ctx, Conditional<Entry> value) {
    // TODO Auto-generated method stub
  }

}
