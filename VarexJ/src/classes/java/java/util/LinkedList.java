package java.util;

@SuppressWarnings("serial")
public class LinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable {

	public LinkedList() {
	}

	@Override
	public void addFirst(E e) {
		throw new RuntimeException();
	}

	@Override
	public void addLast(E e) {
		throw new RuntimeException();

	}

	@Override
	public boolean offerFirst(E e) {
		throw new RuntimeException();
		// return false;
	}

	@Override
	public boolean offerLast(E e) {
		throw new RuntimeException();
		// return false;
	}

	@Override
	public E removeFirst() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E removeLast() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E pollFirst() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E pollLast() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E getFirst() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E getLast() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E peekFirst() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E peekLast() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		throw new RuntimeException();
		// return false;
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		throw new RuntimeException();
		// return false;
	}

	@Override
	public boolean offer(E e) {
		throw new RuntimeException();
		// return false;
	}

	@Override
	public E remove() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E poll() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E element() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public E peek() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public void push(E e) {
		throw new RuntimeException();

	}

	@Override
	public Object clone() {
		throw new RuntimeException();
	}

	@Override
	public E pop() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public Iterator<E> descendingIterator() {
		throw new RuntimeException();
		// return null;
	}

	@Override
	public native boolean add(E e);

	@Override
	public native E get(int index);

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListItr(index);
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public native int size();

	private int nextIndex;
	private int expectedModCount = modCount;

	private class ListItr implements ListIterator<E> {
        private int nextIndex;

        ListItr(int index) {
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size();
        }

        public E next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();
            return get(nextIndex++);
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            return get(nextIndex--);
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            throw new RuntimeException();
        }

        public void set(E e) {
        	throw new RuntimeException();
        }

        public void add(E e) {
            throw new RuntimeException();
        }

        final void checkForComodification() {
        }
    }
}
