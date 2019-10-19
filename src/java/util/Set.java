package java.util;

/**
 * Set:为Collection另一条分支集合，Set的特点是无序，不重复。Set集合之所以可以无序不重复，原因的底层的容器是借用Map来实现的。
 * Set的大部分操作方法都是直接点Map中的方法来实现的
 *
 * @param <E>
 */
public interface Set<E> extends Collection<E> {
    // Query Operations


    int size();


    boolean isEmpty();


    boolean contains(Object o);


    Iterator<E> iterator();


    Object[] toArray();


    <T> T[] toArray(T[] a);


    // Modification Operations


    boolean add(E e);


    boolean remove(Object o);


    // Bulk Operations


    boolean containsAll(Collection<?> c);


    boolean addAll(Collection<? extends E> c);


    boolean retainAll(Collection<?> c);


    boolean removeAll(Collection<?> c);


    void clear();


    // Comparison and hashing


    boolean equals(Object o);


    int hashCode();


    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT);
    }
}
