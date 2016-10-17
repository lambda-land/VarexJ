package cmu.conditional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;

/**
 * Representation of a values that depend on {@link FeatureExpr}.
 * 
 * @author Jens Meinicke
 *
 */
public abstract class Conditional<T> {
	
	public static FeatureModel fm = null;
	
	public static void setFM(final String fmfile) {
		fm = fmfile.isEmpty() ? null : FeatureExprFactory.dflt().featureModelFactory().createFromDimacsFile(fmfile);
		map.clear();
	}
 	
	private static Map<FeatureExpr, Boolean> map = new HashMap<>();
	
	
	public static boolean isContradiction(final FeatureExpr f) {
		if (!map.containsKey(f)) {
			if (f.isContradiction()) {
				map.put(f, Boolean.TRUE);
			} else if (f.isTautology()) {
				map.put(f, Boolean.FALSE);
			} else if (fm != null) {
				map.put(f, f.isContradiction(fm));
			} else {
				map.put(f, Boolean.FALSE);
			}
		}
		return map.get(f);
	}
	
	public static boolean isTautology(final FeatureExpr f) {
		return isContradiction(f.not());
	}
	
	public abstract T getValue();
	public abstract T getValue(boolean ignore);
	
//	protected static final FeatureExpr True = FeatureExprFactory.True();
	
//  def map[U](f: T => U): Conditional[U] = mapr(x => One(f(x)))
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <U> Conditional<U> map(final Function<T,U> f) {
		return mapfr(null, new BiFunction<FeatureExpr, T, Conditional<U>>() {

			public Conditional<U> apply(final FeatureExpr c, T x) {
				return new One(f.apply(x));
			}
			
		});
	}
	
//  def mapr[U](f: T => Conditional[U]): Conditional[U] = mapfr(True, (c, x) => f(x))
	public<U> Conditional<U> mapr(final Function<T, Conditional<U>> f) {
		return mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, T, Conditional<U>>() {

			public Conditional<U> apply(final FeatureExpr c, final T x) {
				return f.apply(x);
			}
			
		});
	}

	
//	def mapf[U](inFeature: FeatureExpr, f: (FeatureExpr, T) => U): Conditional[U] = mapfr(inFeature, (c, x) => One(f(c, x)))
	public <U> Conditional<U> mapf(FeatureExpr inFeature, final BiFunction<FeatureExpr, T, Conditional<U>> f) {
		return mapfr(inFeature, new BiFunction<FeatureExpr, T, Conditional<U>>() {

			public Conditional<U> apply(final FeatureExpr c, final T x) {
				return f.apply(c, x);
			}
			
		});
	}
	public void mapf(FeatureExpr inFeature, final VoidBiFunction<FeatureExpr, T> f) {
		mapfr(inFeature, new VoidBiFunction<FeatureExpr, T>() {

			public void apply(final FeatureExpr c, final T x) {
				f.apply(c, x);
			}
			
		});
	}
	
//	def mapfr[U](inFeature: FeatureExpr, f: (FeatureExpr, T) => Conditional[U]): Conditional[U]
	public abstract <U> Conditional<U> mapfr(FeatureExpr inFeature, BiFunction<FeatureExpr, T, Conditional<U>> f);
	public abstract void mapfr(FeatureExpr inFeature, VoidBiFunction<FeatureExpr, T> f);
	
	public abstract Conditional<T> simplifyValues();
	
	public Conditional<T> simplify(){
		return simplify(FeatureExprFactory.True());
	}
		
	public abstract Conditional<T> simplify(FeatureExpr ctx);
	
	public abstract List<T> toList();
	
	public Map<T, FeatureExpr> toMap() {
		Map<T,FeatureExpr> map = new HashMap<>();
		toMap(FeatureExprFactory.True(), map);
		return map;
	}
    
    protected abstract void toMap(FeatureExpr f, Map<T, FeatureExpr> map);
	
    @Override
	public abstract Conditional<T> clone() throws CloneNotSupportedException;
    
	public static String getCTXString(FeatureExpr ctx) {
		boolean oneSample = ctx instanceof BDDFeatureExpr && ((BDDFeatureExpr)ctx).bdd().pathCount() > 1000; 
		if (oneSample) {
			ctx = new BDDFeatureExpr(((BDDFeatureExpr)ctx).bdd().satOne());
		}
		String context = ctx.toString().replaceAll("CONFIG_", "").replaceAll("__SELECTED_FEATURE_", "").replaceAll("def\\(", "").replaceAll("\\)", "").replaceAll("\\(", "");
		if (oneSample) {
			context = context + " | ...";
		} else if ((context.length() > 300 && context.contains("|"))) {
			context = (context.substring(0, context.indexOf('|')) + " | ...");
		}
		
		return context;
	}

    public abstract int size();
    
    public boolean isOne() {
    	return false;
    }
    
    public FeatureExpr getFeatureExpr(final T t) {
        Conditional<Boolean> c = mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, T, Conditional<Boolean>>() {
            public Conditional<Boolean> apply(final FeatureExpr cc, final T x) {
                if(x == t) return One.TRUE;
                else return One.FALSE;
            }
        });
        FeatureExpr ret = c.toMap().get(true);
        if(ret == null) return FeatureExprFactory.False();
        return ret;
    }
    
    public Conditional<T>[] split(FeatureExpr ctx) {
    	return new Conditional[]{simplify(ctx), simplify(ctx.not())};
    }
    
    public <U,Y> Conditional<U> fastApply(final Conditional<Y> rhs, final BiFunction<T, Y, Conditional<U>> f) {
    	if(rhs instanceof One) {
    		return this.mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, T, Conditional<U>>() {
    			public Conditional<U> apply(final FeatureExpr cc, final T x) {
    				return f.apply(x, rhs.getValue());
    			}
    		});
    	}
    	
    	return this.mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, T, Conditional<U>>() {
			public Conditional<U> apply(final FeatureExpr c, final T x) {
				return rhs.simplify(c).mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, Y, Conditional<U>>() {
					public Conditional<U> apply(final FeatureExpr cc, final Y y) {
						return f.apply(x, y);
					}
				});
			}
		});
    }
    
    public Conditional<T> fastUpdate(final Function<T,  Conditional<T>> f) {
        return  mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, T, Conditional<T>>() {
            public Conditional<T> apply(final FeatureExpr c, final T x) {
                if(isContradiction(c)) return new One<>(x);
                return f.apply(x);
            }
        });
    }

    protected Conditional<T> fastUpdate(FeatureExpr path, FeatureExpr ctx, final Function<T,  Conditional<T>> f) {
        return  mapfr(ctx, new BiFunction<FeatureExpr, T, Conditional<T>>() {
            public Conditional<T> apply(final FeatureExpr c, final T x) {
                if(isContradiction(c)) return new One<>(x);
                return f.apply(x);
            }
        });
    }

    public Conditional<T> fastUpdate(FeatureExpr ctx, final Function<T,  Conditional<T>> f) {
        return  mapfr(ctx, new BiFunction<FeatureExpr, T, Conditional<T>>() {
            public Conditional<T> apply(final FeatureExpr c, final T x) {
                if(isContradiction(c)) return new One<>(x);
                return f.apply(x);
            }
        });
    }
    
    public int depth() {
        throw new UnsupportedOperationException();
    }
}
