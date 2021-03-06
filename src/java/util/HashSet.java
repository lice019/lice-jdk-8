package java.util;

import java.io.InvalidObjectException;

import sun.misc.SharedSecrets;


/**
 * HashSet:底层是Hash表实现的，实际是使用了HashMap作为容器，所以拥有了Hash的功能和不可重复，无序的
 *
 * @param <E>
 */
public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable {

    static final long serialVersionUID = -5024744406713321676L;

    //使用HashMap作为容器
    private transient HashMap<E, Object> map;

    // Dummy value to associate with an Object in the backing Map
    //与支持映射中的对象关联的虚值
    private static final Object PRESENT = new Object();


    //在实例化HashSet对象时，创建了HashMap实例作为容器
    public HashSet() {
        map = new HashMap<>();
    }


    public HashSet(Collection<? extends E> c) {
        //分配了HashMap的容器大小
        map = new HashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
        //将c集合中元素添加到HashMap容器中
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

    //获取HashSet的迭代器，实际上是HashMap中KeySet的迭代器
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }


    public int size() {
        return map.size();
    }


    public boolean isEmpty() {
        return map.isEmpty();
    }


    public boolean contains(Object o) {
        return map.containsKey(o);
    }


    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }


    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }


    public void clear() {
        map.clear();
    }


    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet<E>) super.clone();
            newSet.map = (HashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    //序列化将对象写到物理内存中
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out HashMap capacity and load factor
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());

        // Write out size
        s.writeInt(map.size());

        // Write out all elements in the proper order.
        for (E e : map.keySet())
            s.writeObject(e);
    }

    //序列化将对象从物理内存中读取到JVM内存中
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read capacity and verify non-negative.
        int capacity = s.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Illegal capacity: " +
                    capacity);
        }

        // Read load factor and verify positive and non NaN.
        float loadFactor = s.readFloat();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " +
                    loadFactor);
        }

        // Read size and verify non-negative.
        int size = s.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Illegal size: " +
                    size);
        }
        // Set the capacity according to the size and load factor ensuring that
        // the HashMap is at least 25% full but clamping to maximum capacity.
        capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
                HashMap.MAXIMUM_CAPACITY);

        // Constructing the backing map will lazily create an array when the first element is
        // added, so check it before construction. Call HashMap.tableSizeFor to compute the
        // actual allocation size. Check Map.Entry[].class since it's the nearest public type to
        // what is actually created.

        SharedSecrets.getJavaOISAccess()
                .checkArray(s, Map.Entry[].class, HashMap.tableSizeFor(capacity));

        // Create backing HashMap
        map = (((HashSet<?>) this) instanceof LinkedHashSet ?
                new LinkedHashMap<E, Object>(capacity, loadFactor) :
                new HashMap<E, Object>(capacity, loadFactor));

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) s.readObject();
            map.put(e, PRESENT);
        }
    }


    public Spliterator<E> spliterator() {
        return new HashMap.KeySpliterator<E, Object>(map, 0, -1, 0, 0);
    }
}
