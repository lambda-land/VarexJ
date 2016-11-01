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
public class JPF_java_util_LinkedList extends NativePeer  {
    final Map<Integer, OList> myLists = new HashMap<>();

	@MJI
	public void $init____V(MJIEnv env, int objref, FeatureExpr ctx) {
		myLists.put(objref, new OList());
	}
  

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		OList list = myLists.get(objref);
		return list.length();
		
	}

	@MJI
	public boolean add__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		OList list = myLists.get(objref);
		list.add(argRef, ctx);
		myLists.put(objref, list);
		return true;// always true
	}

	@MJI
	public Conditional<Integer> get__I__Ljava_lang_Object_2(final MJIEnv env, int objref, final int index, FeatureExpr ctx) {
		OList list = myLists.get(objref);
		return list.get(index, ctx);
	}
}
class OList {

	private class Entry {
		Integer key;
		FeatureExpr value;
		Entry next;

		public Entry() {
		}

		public Entry(Integer key, FeatureExpr value) {
			this.key = key;
			this.value = value;
			this.next = null;
		}

		public Conditional<Integer> length() {
			Conditional<Integer> t;
			if (this.next == null)
				t = new One<>(0);
			else
				t = this.next.length();
			t = t.mapf(this.value, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
				@Override
				public Conditional<Integer> apply(FeatureExpr ctx, Integer x) {
					// @SuppressWarnings("unchecked")
					return ChoiceFactory.create(ctx, new One<>(x + 1), new One<>(x));
				}
			}).simplify();
			return t;
		}
	}

	static public Conditional<Integer> getEntry(Entry e, int index) {
		if (e == null)
			return new One<>(0);
		if (index == 0) {
			return ChoiceFactory.create(e.value, new One<>(e.key), getEntry(e.next, index));
		} else {
			return ChoiceFactory.create(e.value, getEntry(e.next, index - 1), getEntry(e.next, index));
		}
	}

	Entry table;
	public int size = 0;

	public OList() {
		table = null;
	}

	public void add(Integer key, FeatureExpr ctx) {
		Entry node = new Entry(key, ctx);
		Entry cur = this.table;

		while (cur != null) {
			if (cur.key == key) {
				cur.value = cur.value.or(ctx);
				size++;
				return;
			}
			cur = cur.next;
		}

		node.next = table;
		table = node;
		size++;

	}

	public FeatureExpr contains(Integer key) {
		Entry cur = table;
		while (cur != null) {
			if (cur.key == key) {
				return cur.value;
			}
			cur = cur.next;
		}
		return null;
	}

	public Conditional<Integer> get(int index, FeatureExpr ctx) {
		return getEntry(table, index).simplify(ctx);
	}

	public boolean isEmpty() {
		return (this.table == null);
	}

	public Conditional<Integer> length() {
		if (table == null)
			return new One<>(0);
		return table.length();
	}
}