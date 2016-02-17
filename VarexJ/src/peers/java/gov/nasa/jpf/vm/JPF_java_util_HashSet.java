package gov.nasa.jpf.vm;

import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.sat.True;
import gov.nasa.jpf.annotation.MJI;

public class JPF_java_util_HashSet extends NativePeer {

	// XXX peer method names are defined as: name__parameterTypes__returntype
	// I:int, V:void, Z: boolean ...
	// you can replace the return value and the parameters by the corresponding
	// Conditional<Type>

	final Map<Integer, VSet> mySet = new HashMap<>();

	@MJI
	public void $init____V(MJIEnv env, int objref, FeatureExpr ctx) {
		VSet setList = mySet.get(objref);
		if (setList == null) {
			mySet.put(objref, new VSet());
		}
	}

	@MJI
	public boolean add__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		set.add(argRef, ctx);
		mySet.put(objref, set);
		return true;// always true
	}

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		return set.length().simplify(ctx);

	}

	@MJI
	public Conditional<Integer> get__I__Ljava_lang_Object_2(final MJIEnv env, int objref, final int index,
			FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		return set.get(index, ctx);
	}

	@MJI
	public void printf____V(final MJIEnv env, int objref, FeatureExpr ctx) {
		VSet set = mySet.get(objref);

	}

	// SeparateChainingHashTable<E> is a genaric implementation of the hash
	// table ADT using separate chaining to avoid collisions.
	public class SeparateChainingHashTable<E> {
		private static final int DEFAULT_PRIME = 11; // default number of buckets

		private Node[] buckets; // elements in this hash table
		private int size; // number of elements in this hash table

		// post: construct empty hash table with default number of buckets
		public SeparateChainingHashTable() {
			this(DEFAULT_PRIME);
		}

		// pre : n > 1
		// post: construct empty hash table with p buckets where n <= p < 2 * n
		@SuppressWarnings("unchecked")
		public SeparateChainingHashTable(int n) {
			if (n <= 1) {
				throw new IllegalArgumentException();
			}
			int p = prime(n);
			buckets = (Node[]) new SeparateChainingHashTable.Node[p];
			size = 0;
		}

		// post: insert given value into this hash table if it does not contain
		// given value
		public void insert(E value) {
			// rehash if 3 / 4 < size / buckets.length (RHS is load factor and
			// denoted by lambda)
			if (3 * buckets.length < 4 * size) {
				rehash();
			}
			// separate chaining to avoid collisions
			if (!contains(value)) {
				int i = hash(value);
				Node node = new Node(value);
				node.next = buckets[i];
				buckets[i] = node;
				size++;
			}
		}

		// post: remove given value from this hash table if it contains given
		// value
		public void remove(E value) {
			int i = hash(value);
			if (buckets[i] != null) {
				if (buckets[i].data.equals(value)) {
					// value at front of list
					buckets[i] = buckets[i].next;
					size--;
				} else {
					// value not at front of list
					Node current = buckets[i];
					while (current.next != null && !current.next.data.equals(value)) {
						// value not next in list
						current = current.next;
					}
					// assertion: current.next == null ||
					// current.next.data.equals(value)
					if (current.next != null) {
						// value next in list
						current.next = current.next.next;
						size--;
					}
				}
			}
		}

		// post: return true if given value is contained in this hash table,
		// false otherwise
		public boolean contains(E value) {
			int i = hash(value);
			Node current = buckets[i];
			while (current != null) {
				if (current.data.equals(value)) {
					// value at current node
					return true;
				}
				// value not at current node
				current = current.next;
			}
			// value not contained in this hash table
			return false;
		}

		// post: return number of elements in this hash table
		public int size() {
			return size;
		}

		// post: return string representation of this hash table
		public String toString() {
			if (size == 0) {
				return "[]";
			} else {
				// find first list
				int i = 0;
				while (buckets[i] == null) {
					i++;
				}
				// print elements in first list
				Node current = buckets[i];
				String result = "[" + current.data;
				while (current.next != null) {
					current = current.next;
					result = result + ", " + current.data;
				}
				// find and print elements in all other lists
				for (int j = i + 1; j < buckets.length; j++) {
					current = buckets[j];
					while (current != null) {
						result = result + ", " + current.data;
						current = current.next;
					}
				}
				result = result + "]";
				return result;
			}
		}

		// print hash table to output
		// NOTE: output is formatted "nicely" if values are no more than six
		// characters long
		public void debug() {
			System.out.println("debug output");
			System.out.printf("index: data:\n");
			for (int i = 0; i < buckets.length; i++) {
				// print row for list of values
				System.out.printf("%-4d   ", i);
				Node node = buckets[i];
				if (node == null) {
					System.out.printf("%s\n", "null");
				} else {
					System.out.printf("%-6s", node.data);
					node = node.next;
					while (node != null) {
						System.out.printf(" --> %-6s", node.data);
						node = node.next;
					}
					System.out.println();
				}
			}
			System.out.println("size: " + size);
			System.out.println();
		}

		// rehash this hash table
		@SuppressWarnings("unchecked")
		private void rehash() {
			Node[] temp = buckets;
			int p = prime(2 * buckets.length);
			buckets = (Node[]) new SeparateChainingHashTable.Node[p];
			size = 0;
			for (int i = 0; i < temp.length; i++) {
				Node node = temp[i];
				while (node != null) {
					insert(node.data);
					node = node.next;
				}
			}
		}

		// hash function for this hash table
		private int hash(E value) {
			return Math.abs(value.hashCode()) % buckets.length;
		}

		// TODO: inner class comment
		private class Node {
			private E data;
			private Node next;

			public Node(E value) {
				data = value;
				next = null;
			}
		}

		// pre : n > 1
		// post: return smallest prime greater than or equal to given number
		private static int prime(int n) {
			// TODO: check preconditions
			// use sieve of Eratosthenes to find all primes p with 2 <= p < 2 *
			// n
			Queue<Integer> queue = new LinkedList<Integer>();
			Stack<Integer> stack = new Stack<Integer>();
			// add all integers i with 2 <= i < 2 * n to queue
			for (int i = 2; i < 2 * n; i++) {
				queue.add(i);
			}
			// add all primes p with p < 2 * n to stack
			while (!queue.isEmpty()) {
				// remove next largest prime from front of queue
				int p = queue.remove();
				// push p onto stack of prime numbers
				stack.push(p);
				// remove multiples of p from queue
				Iterator<Integer> iterator = queue.iterator();
				while (iterator.hasNext()) {
					int i = iterator.next();
					if (i % p == 0) {
						// p divides i
						iterator.remove();
					}
				}
			}
			// find smallest prime p with n <= p
			// there exists prime p such that n < p < 2 * n by Bertrand's
			// theorem
			// assertion: !stack.isEmpty()
			int p = stack.pop();
			// assertion: !stack.isEmpty()
			int q = stack.pop();
			while (q >= n) {
				// assertion: !stack.isEmpty()
				p = q;
				q = stack.pop();
			}
			return p;
		}
	}
}

class VSet {

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

	public VSet() {
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
