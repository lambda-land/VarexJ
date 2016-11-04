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
import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Types;

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
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append(stack);

		string.append("\nBuffered: " + bufferCTX);
		int i = buffer.size();
		for (Tuple e : buffer) {
			string.append('\n');
			string.append(--i);
			string.append(':');
			string.append(e);
		}

		return string.toString();
	}
	public BufferedStack(int nOperands ) {
	
		maxStackSize = nOperands;
		stack = new ConditionalStack(nOperands);
		stack.setCtx(FeatureExprFactory.True());
	}
	
	public BufferedStack() {
		this.maxStackSize = 0;
	}
	@Override
	public int getStackWidth() {
		return stack.getStackWidth();
	}

	@Override
	public FeatureExpr getCtx() {
		return stack.getCtx();
	}

	@Override
	public void setCtx(FeatureExpr ctx) {
		stack.setCtx(ctx);

	}

	@Override
	public boolean hasAnyRef(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (bufferCTX.equivalentTo(ctx)) {
				for (Tuple entry : buffer) {
					if (entry.isRef) {
						return true;
					}
				}
				return stack.hasAnyRef(ctx);
			}
			debufferAll();
		}
		return stack.hasAnyRef(ctx);
	}
	
	//TODO: modify
	@Override
	public boolean isRefLocal(FeatureExpr ctx, int index) {
		debufferAll();
		return stack.isRefLocal(ctx, index);
	}

	@Override
	public Conditional<Integer> getTop() {
		if (!buffer.isEmpty()) {
			int size = -1;
			for (Tuple entry : buffer) {
				Conditional value = entry.value;
				if (value.getValue(true) instanceof Integer) {
					size++;
					continue;
				}
				if (value.getValue(true) instanceof Float) {
					size++;
					continue;
				}
				if (value.getValue(true) instanceof Long) {
					size += 2;
					continue;
				}
				if (value.getValue(true) instanceof Double) {
					size += 2;
					continue;
				}
			}

			Conditional<Integer> stackTop = stack.getTop();
			if (stackTop.equals(One.valueOf(-1))) {
				if (Conditional.isTautology(bufferCTX)) {
					return One.valueOf(size);
				}
				return ChoiceFactory.create(bufferCTX, new One<>(size), new One<>(-1));
			}
			final int finalSize = size;
			return stackTop.mapf(FeatureExprFactory.True(), new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {

				@Override
				public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
					FeatureExpr context = bufferCTX.and(ctx);
					if (Conditional.isContradiction(context)) {
						return One.valueOf(y);
					}
					return ChoiceFactory.create(context, new One<>(y + finalSize + 1), new One<>(y));
				}

			}).simplify();
		}
		return stack.getTop();
	}

	@Override
	public int[] getSlots(FeatureExpr ctx) {
		debufferAll();
		return stack.getSlots(ctx);
	}

	@Override
	public Set<Integer> getAllReferences() {
		Set<Integer> references = stack.getAllReferences();
		for (Tuple entry : buffer) {
			if (entry.isRef) {
				references.addAll(entry.value.toList());
			}
		}
		return references;
	}

	@Override
	public Conditional<Integer> getInteger(FeatureExpr ctx, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (bufferCTX.equivalentTo(ctx)) {
				buffer.clear();
				bufferCTX = FeatureExprFactory.True();
			} else {
				debufferAll();
			}
		}
		stack.clear(ctx);
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
	
	
	

	
	public Conditional<Integer> peek(FeatureExpr ctx, final int offset) {
		if (!buffer.isEmpty()) {
			if (getBufferSize() > offset && bufferCTX.equivalentTo(ctx)) {
				int pointer = offset;
				int n = 0;
				while (n <= pointer) {
					Conditional value = buffer.get(n).value;
					Object type = value.getValue(true);
					if (type instanceof Integer) {
						if (n == offset) {
							return value;
						}
						n++;
						continue;
					}
					if (type instanceof Float) {
						if (n == offset) {
							return value.map(new Function<Float, Integer>() {

								@Override
								public Integer apply(Float x) {
									return Types.floatToInt(x.floatValue());
								}

							});
						}
						n++;
						continue;
					}
					if (type instanceof Double || type instanceof Long) {
						if (n - pointer > 1) {
							n++;
							pointer--;
							continue;
						} else {
							debufferAll();
							return stack.peek(ctx, offset, Type.DOUBLE);
						}
					}
					if (type instanceof Byte) {
						if (n == offset) {
							return value.map(new Function<Byte, Integer>() {

								@Override
								public Integer apply(Byte x) {
									return (int)x.byteValue();
								}
								
							});
						}
						n++;
						continue;
					}
					if (type instanceof Short) {
						if (n == offset) {
							return value.map(new Function<Short, Integer>() {

								@Override
								public Integer apply(Short x) {
									return (int)x;
								}
								
							});
						}
						n++;
						continue;
					}
					throw new RuntimeException("type " + type.getClass() + " missed");
					
				}
			}
			debufferAll();
		}
		return stack.peek(ctx, offset, Type.INT);
	}
     
	private int getBufferSize() {
		int size = 0;
		for (Tuple entry : buffer) {
			Object type = entry.value.getValue(true);
			if (type instanceof Integer ||
				type instanceof Float ||
				type instanceof Byte ||
				type instanceof Short) {
				size++;
			} else {
				size += 2;
			}
		}
		
		
		return size;
	}
	@Override
	public <T> Conditional<T> peek(FeatureExpr ctx, final int offset, Type t) {
		if (!buffer.isEmpty()) {
			if (getBufferSize() > offset && bufferCTX.equivalentTo(ctx)) {
				int pointer = offset;
				int n = 0;
				while (n <= pointer) {
					Conditional value = buffer.get(n).value;
					Object type = value.getValue(true);
					if (type instanceof Integer) {
						if (n == offset) {
							switch (t) {
							
							case LONG:
								if (buffer.size() > n + 1) { 
									final Conditional value2 = buffer.get(n + 1).value;
									return value.mapr(new Function<Integer, Conditional<Long>>() {
	
										@Override
										public Conditional<Long> apply(final Integer x1) {
											return value2.map(new Function<Integer, Long>() {
	
												@Override
												public Long apply(Integer x2) {
													return Types.intsToLong(x1, x2);
												}
	
											});
										}
	
									});
								} else {
									break;
								}
							default:
								throw new RuntimeException("type " + type.getClass() + " missed" + t);
							}
						}
						n++;
						continue;
					}
					if (type instanceof Float) {
						if (n == offset) {
							switch (t) {
							case FLOAT:
								return value;
							case INT:
								return value.map(new Function<Float, Integer>() {

									@Override
									public Integer apply(Float x) {
										return Types.floatToInt(x.floatValue());
									}

								});
							default:
								break;
							}
							
						}
						n++;
						continue;
					}
					if (type instanceof Double || type instanceof Long) {
						if (n - pointer > 1) {
							n++;
							pointer--;
							continue;
						} else {
							debufferAll();
							return stack.peek(ctx, offset, t);
						}
					}
					if (type instanceof Byte) {
						if (n == offset) {
							return value.map(new Function<Byte, Integer>() {

								@Override
								public Integer apply(Byte x) {
									return (int)x.byteValue();
								}
								
							});
						}
						n++;
						continue;
					}
					if (type instanceof Short) {
						if (n == offset) {
							return value.map(new Function<Short, Integer>() {

								@Override
								public Integer apply(Short x) {
									return (int)x;
								}
								
							});
						}
						n++;
						continue;
					}
					throw new RuntimeException("type " + type.getClass() + " missed");
					
				}
			}
			debufferAll();
		}
		return stack.peek(ctx, offset, t);
	}

	@Override
	public Conditional<Entry> popEntry(FeatureExpr ctx, final boolean copyRef) {
		if (!buffer.isEmpty()) {
			if (bufferCTX.equivalentTo(ctx)) {
				final Object value = buffer.peek().value.getValue(true);
				final Tuple v = buffer.pop();
				return v.value.map(new Function<Integer, Entry>(){
					public Entry apply(Integer value) {
						if(copyRef) return new Entry(value, v.isRef);
						return new Entry(value, false);
					}
				});
			} else {
				debufferAll();
			}
		}
		return stack.popEntry(ctx, copyRef);
	}

	



	@Override
	public boolean isRef(FeatureExpr ctx, int offset) {
		if (!buffer.isEmpty()) {
			if (bufferCTX.equivalentTo(ctx)) {
				if (buffer.size() > offset) {
					return buffer.get(offset).isRef;
				} else {
					return stack.isRef(ctx, offset - buffer.size());
				}
			}
			debufferAll();
		}
		return stack.isRef(ctx, offset);
	}

	@Override
	public void set(FeatureExpr ctx, int offset, int value, boolean isRef) {
		debufferAll();
		stack.set(ctx, offset, value, isRef);
	}

	@Override
	public void setTop(FeatureExpr ctx, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public IVStack clone() {
		BufferedStack clone = new BufferedStack();
		clone.maxStackSize = maxStackSize;
		clone.buffer = (LinkedList<Tuple>) buffer.clone();
		clone.bufferCTX = bufferCTX;
		clone.stack = this.stack.clone();
		clone.stackCTX = this.stackCTX;
		return clone;
	}
	
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if(!(o instanceof BufferedStack)) {
        	return false;
        }
        if(this.stack instanceof ConditionalStack && ((BufferedStack)o).stack  instanceof ConditionalStack) {
        	((BufferedStack)o).debufferAll();
            this.debufferAll();
            return stack.equals(((BufferedStack)o).stack);
        }
        if(this.stack instanceof VStack && ((BufferedStack)o).stack  instanceof VStack) {
        	((BufferedStack)o).debufferAll();
            this.debufferAll();
            return stack.equals(((BufferedStack)o).stack);
        }
        return false;
    }


	// A => A A
	@Override
	public void dup(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (bufferCTX.equivalentTo(ctx)) {
				Tuple peek = buffer.peek();
				if (peek.value.getValue(true) instanceof Integer) {
					buffer.push(peek);
					return;
				}
				debufferAll();
			} else {
				debufferAll();
			}
		}

		stack.dup(ctx);
	}

	// A B => A B A B
	@Override
	public void dup2(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (buffer.size() >= 2 && bufferCTX.equivalentTo(ctx)) {
				if (buffer.peek().value.getValue(true) instanceof Integer &&
						buffer.get(1).value.getValue(true) instanceof Integer) {
					buffer.push(buffer.get(1));
					buffer.push(buffer.get(1));
					return;
				}
			}
			debufferAll();
		}
		stack.dup2(ctx);
	}

	// A B => B A
	@Override
	public void swap(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (buffer.size() >= 2 && bufferCTX.equivalentTo(ctx)) {
				if (buffer.peek().value.getValue(true) instanceof Integer &&
					buffer.get(1).value.getValue(true) instanceof Integer) {
					buffer.add(1, buffer.pop());
					return;
				}
			}
			debufferAll();
		}
		stack.swap(ctx);
	}

	// A B C => .. B C A B C
	@Override
	public void dup2_x1(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (buffer.size() >= 3 && bufferCTX.equivalentTo(ctx)) {
				if (buffer.peek().value.getValue(true) instanceof Integer && buffer.get(1).value.getValue(true) instanceof Integer && buffer.get(2).value.getValue(true) instanceof Integer) {
					buffer.add(3, buffer.get(1));
					buffer.add(3, buffer.peek());
					return;
				}
			}
			debufferAll();
		}
		stack.dup2_x1(ctx);
	}

	// A B C D => .. C D A B C D
	@Override
	public void dup2_x2(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (buffer.size() >= 4 && bufferCTX.equivalentTo(ctx)) {
				if (buffer.peek().value.getValue(true) instanceof Integer && buffer.get(1).value.getValue(true) instanceof Integer && buffer.get(2).value.getValue(true) instanceof Integer
						&& buffer.get(3).value.getValue(true) instanceof Integer) {
					buffer.add(4, buffer.get(1));
					buffer.add(4, buffer.peek());
					return;
				}
			}
			debufferAll();
		}
		stack.dup2_x2(ctx);
	}

	// A B => .. B A B
	@Override
	public void dup_x1(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (buffer.size() >= 2) {
				if (bufferCTX.equivalentTo(ctx)) {
					if (buffer.peek().value.getValue(true) instanceof Integer && buffer.get(1).value.getValue(true) instanceof Integer) {
						buffer.add(2, buffer.peek());
						return;
					}
				}
			}
			debufferAll();
		}
		stack.dup_x1(ctx);
	}

	// A B C => .. C A B C
	@Override
	public void dup_x2(FeatureExpr ctx) {
		if (!buffer.isEmpty()) {
			if (buffer.size() >= 3 && bufferCTX.equivalentTo(ctx)) {
				buffer.add(3, buffer.peek());
				return;
			} else {
				debufferAll();
			}
		}
		stack.dup_x2(ctx);
	}


  @Override
  public void pushEntry(FeatureExpr ctx, Conditional<Entry> value) {
	    Conditional<Boolean> ref  = value.map(new Function<Entry, Boolean>() {
	    	public Boolean apply(Entry value) {
	    		return value.isRef;
	    	}
	    }).simplify(ctx);
	    
	    Conditional<Integer> val = value.map(new Function<Entry, Integer>() {
	    	public Integer apply(Entry value) {
	    		return value.value;
	    	}
	    }).simplify(ctx);
	    
	    if(!(ref instanceof One)) {
	    	debufferAll();
	    	stack.pushEntry(ctx, value);
	    }
	    
		if (buffer.isEmpty()) {
			bufferCTX = ctx;
			addToBuffer(val, ref.getValue());
		} else if (ctx.equivalentTo(bufferCTX)) {
			addToBuffer(val, ref.getValue());
		} else {
			debufferAll();
			bufferCTX = ctx;
			addToBuffer(val, ref.getValue());
		}
  }
@Override
public Conditional<Stack> getStack() {
	// TODO Auto-generated method stub
	return null;
}

}
