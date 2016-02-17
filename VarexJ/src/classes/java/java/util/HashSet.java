package java.util;

public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable {
	static final long serialVersionUID = -5024744406713321676L;

	private transient HashMap<E, Object> map;

	private static final Object PRESENT = new Object();

	public HashSet() {
	}

	public HashSet(Collection<? extends E> c) {
		map = new HashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	public HashSet(int initialCapacity, float loadFactor) {
		map = new HashMap<>(initialCapacity, loadFactor);
	}

	public HashSet(int initialCapacity) {
		map = new HashMap<>(initialCapacity);
	}

	HashSet(int initialCapacity, float loadFactor, boolean dummy) {
		map = new LinkedHashMap<>(initialCapacity, loadFactor);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public native boolean contains(Object o);

	@Override
	public native boolean add(E e);
	
	@Override
	public boolean remove(Object o) {
		throw new RuntimeException();
	}

	@Override
	public void clear() {
		throw new RuntimeException();
	}

	@Override
	public Object clone() {
		throw new RuntimeException();
	}

	public native E get(int index);

	@Override
	public Iterator<E> iterator() {
		return new HashSetIterator();
	}

	@Override
	public native int size();

	private class HashSetIterator implements Iterator<E> {
		private int position;
		private boolean removeOK;

		public HashSetIterator() {
			position = 0;
			removeOK = false;
		}

		public boolean hasNext() {
			return position < size();
		}

		public E next() {
			checkForComodification();
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			E result = get(position);
			position++;
			removeOK = true;
			return result;
		}

		public void remove() {
			if (!removeOK) {
				throw new IllegalStateException();
			}
			E key = get(position - 1);
			HashSet.this.remove(key);
			position--;
			removeOK = false;
		}

		final void checkForComodification() {
			// FIXME: what is this method for?
		}
	}

}
