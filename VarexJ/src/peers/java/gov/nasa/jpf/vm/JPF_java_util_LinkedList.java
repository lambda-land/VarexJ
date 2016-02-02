package gov.nasa.jpf.vm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.annotation.MJI;

/**
 * Trivial implementation of va-LinkedList
 * 
 * @author Jens Meinicke
 *
 */
public class JPF_java_util_LinkedList extends NativePeer {

	// XXX peer method names are defined as: name__parameterTypes__returntype
	// I:int, V:void, Z: boolean ...
	// you can replace the return value and the parameters by the corresponding Conditional<Type>
	
	// TODO replace Conditional<LinkedList<Integer>> by a varaibility-aware data structure
	final Map<Integer, Conditional<LinkedList<Integer>>> myLists = new HashMap<>();

	@MJI
	public void $init____V(MJIEnv env, int objref, FeatureExpr ctx) {
		myLists.put(objref, ChoiceFactory.create(ctx, new One<>(new LinkedList<Integer>()), new One<LinkedList<Integer>>(null)).simplify());
	}

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		Conditional<LinkedList<Integer>> list = myLists.get(objref).simplify(ctx);
		return list.map(new Function<LinkedList<Integer>, Integer>() {

			@Override
			public Integer apply(LinkedList<Integer> x) {
				return x.size();
			}

		}).simplify(ctx);
	}

	@MJI
	public boolean add__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		Conditional<LinkedList<Integer>> list = myLists.get(objref);
		list = list.mapf(ctx, new BiFunction<FeatureExpr, LinkedList<Integer>, Conditional<LinkedList<Integer>>>() {

			@Override
			public Conditional<LinkedList<Integer>> apply(FeatureExpr ctx, LinkedList<Integer> list) {
				@SuppressWarnings("unchecked")
				LinkedList<Integer> clone = (LinkedList<Integer>) list.clone();
				clone.add(argRef);
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(list));

			}

		}).simplify();
		myLists.put(objref, list);
		return true;// always true
	}

	@MJI
	public Conditional<Integer> get__I__Ljava_lang_Object_2(final MJIEnv env, int objref, final int index, FeatureExpr ctx) {
		Conditional<LinkedList<Integer>> list = myLists.get(objref).simplify(ctx);
		return list.mapf(ctx, new BiFunction<FeatureExpr, LinkedList<Integer>, Conditional<Integer>>() {

			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, LinkedList<Integer> list) {
				try {
					return One.valueOf(list.get(index));
				} catch (Exception e) {
					env.ti.createAndThrowException(ctx, e.getClass().getName());
				}
				return One.valueOf(-1);
			}

		});
	}

}
