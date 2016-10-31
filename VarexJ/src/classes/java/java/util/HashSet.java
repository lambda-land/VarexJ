package java.util;
import java.io.*;

public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
    static final long serialVersionUID = -5024744406713321676L;

    private transient HashMap<E,Object> map;

    private static final Object PRESENT = new Object();

    public HashSet() {
    }


 
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
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
    public boolean contains(Object o) {
        //System.out.println("contains " + o);
        if(mycontains(o) == 0) return false;
        else return true;
    }
    
    public native int mycontains(Object o);
    
    @Override
    public boolean isEmpty(){
        if(size() == 0){return true;}
        else return false;
    }
    
    @Override
    public native boolean add(E e);

    @Override
    public native boolean remove(Object o);
    
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
    public Iterator<E> iterator(){
        return new HashItr(0);
    }
    
    @Override
    public native int size();
    
    private class HashItr implements Iterator<E> {
        private int nextIndex;

        public HashItr(int index) {
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

