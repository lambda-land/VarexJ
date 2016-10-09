package cmu.conditional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

/**
 * Choice implementation using a map from value to {@link FeatureExpr}.
 * 
 * @author Jens Meinicke
 *
 * @param <T>
 */
public class MapChoice<T> extends IChoice<T> implements Cloneable {

	Map<T, FeatureExpr> map = new HashMap<>();
	
	MapChoice(FeatureExpr featureExpr, Conditional<T> thenBranch, Conditional<T> elseBranch) {
		super(featureExpr, thenBranch, elseBranch);
		for (Entry<T, FeatureExpr> e : thenBranch.toMap().entrySet()) {
			FeatureExpr and = e.getValue().and(featureExpr);
			if (!isContradiction(and)) {
				if (map.containsKey(e.getKey())) {
					map.put(e.getKey(), map.get(e.getKey()).or(and));
				} else {
					map.put(e.getKey(), and);
				}
			}
		}

		for (Entry<T, FeatureExpr> e : elseBranch.toMap().entrySet()) {
			FeatureExpr andNot = e.getValue().andNot(featureExpr);
			if (!isContradiction(andNot)) {
				if (map.containsKey(e.getKey())) {
					map.put(e.getKey(), map.get(e.getKey()).or(andNot));
				} else {
					map.put(e.getKey(), andNot);
				}
			}
		}
	}

	MapChoice(Map<T, FeatureExpr> newMap) {
		super(newMap);
		map = newMap;
	}

	@Override
	public T getValue() {
		if (map.size() == 1) {
			return map.keySet().iterator().next();
		}
		throw new RuntimeException("get Value on choice called" + map);
	}

	@Override
	public T getValue(final boolean ignore) {
		if (map.isEmpty()) {
			return null;
		}
		return map.keySet().iterator().next();
	}

	@Override
	public <U> Conditional<U> mapfr(FeatureExpr inFeature, BiFunction<FeatureExpr, T, Conditional<U>> f) {
		Map<U, FeatureExpr> newMap = new HashMap<>();
		for (Entry<T, FeatureExpr> e : map.entrySet()) {
			Conditional<U> result = f.apply(inFeature == null ? e.getValue() : e.getValue().and(inFeature), e.getKey());
			if (result == null) {
				newMap.put(null, e.getValue());
				continue;
			}
			for (Entry<U, FeatureExpr> r : result.toMap().entrySet()) {// ??
				FeatureExpr and = r.getValue().and(e.getValue());
				final U key = r.getKey();
				if (newMap.containsKey(key)) {
					newMap.put(key, newMap.get(key).or(and));
				} else {
					if (!isContradiction(and)) {
						newMap.put(key, and);
					}
				}
			}
		}

		return new MapChoice<>(newMap);
	}
	
	@Override
	public void mapfr(FeatureExpr inFeature, VoidBiFunction<FeatureExpr, T> f) {
		for (Entry<T, FeatureExpr> e : map.entrySet()) {
			f.apply(inFeature == null ? e.getValue() : e.getValue().and(inFeature), e.getKey());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Conditional<T> simplifyValues() {
		if (map.size() == 1) {
			T value = map.keySet().iterator().next();
			if (value instanceof Byte) {
				return (Conditional<T>) One.valueOf((Byte)value);
			}
			if (value instanceof Boolean) {
				return (Conditional<T>) One.valueOf((Boolean)value);
			}
			if (value instanceof Integer) {
				return (Conditional<T>) new One<>((Integer)value);
			}
			return new One<>(value);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Conditional<T> simplify(FeatureExpr ctx) {
		if (map.size() == 1) {
			return simplifyValues();
		}
		if (isTautology(ctx)) {
			return this;
		}

		Map<T, FeatureExpr> newMap = new HashMap<>();
		for (Entry<T, FeatureExpr> e : map.entrySet()) {
			final FeatureExpr value = e.getValue();
			if (!isContradiction(value.and(ctx))) {
				T key = e.getKey();
				if (ctx.equals(value)) {
					if (key instanceof Byte) {
						return (Conditional<T>) One.valueOf((Byte)key);
					}
					if (key instanceof Boolean) {
						return (Conditional<T>) One.valueOf((Boolean)key);
					}
					if (key instanceof Integer) {
						return (Conditional<T>) new One<>((Integer)key);
					}
					return new One<>(key);
				} else {
					newMap.put(key, value);
				}
			}
		}

		return new MapChoice<>(newMap);

	}

	@Override
	public List<T> toList() {
		return new ArrayList<>(map.keySet());
	}

	@Override
	protected void toMap(FeatureExpr f, Map<T, FeatureExpr> map) {
		throw new RuntimeException("Should never be called");
	}

	@Override
	public Conditional<T> clone() throws CloneNotSupportedException {
		return new MapChoice<>(map);
	}
	
	@Override
	public int hashCode() {
		return map.hashCode();//throw new RuntimeException("not yet implemented");
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof MapChoice) {
			@SuppressWarnings("rawtypes")
			MapChoice other = (MapChoice) o;
			return other.map.equals(map);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder content = new StringBuilder();
		content.append('{');
		for (Entry<T, FeatureExpr> e: map.entrySet()) {
			content.append(getCTXString(e.getValue()));
			content.append(':');
			content.append(e.getKey());
			content.append(" ; ");
		}
		content.delete(content.length() - 3, content.length() - 1);
		content.append('}');
		return content.toString();
	}

	@Override
	public Map<T, FeatureExpr> toMap() {
		return map;
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
    public FeatureExpr getFeatureExpr(T t) {
		FeatureExpr ret = map.get(t);
		if(ret == null) return FeatureExprFactory.False();
		return ret;
	}
    
	@Override
    public Conditional<T>[] split(FeatureExpr ctx) {
		return new Conditional[] { this.simplify(ctx), this.simplify(ctx.not()) };
	}

}
