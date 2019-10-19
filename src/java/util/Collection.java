package java.util;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Collection：集合中的顶层接口，延伸出list、set、map、vector等类型的集合。
 *
 * @param <E>
 */
public interface Collection<E> extends Iterable<E> {
    // Query Operations---集合查询操作

    //返回集合的元素个数
    int size();

    //判断集合是否为空，不为空，返回true
    boolean isEmpty();

    //集合是否包含o元素
    boolean contains(Object o);

    //集合的迭代器接口--集合的顶层迭代器，可以限定泛型，一旦限定泛型，只能迭代限定类型的元素
    Iterator<E> iterator();

    //集合中元素以数组形式输出
    Object[] toArray();

    //返回包含此集合中所有元素的数组; 返回的数组的运行时类型是指定数组的运行时类型。
    <T> T[] toArray(T[] a);

    // Modification Operations----集合修改操作


    //集合添加元素，添加成功，返回true；失败，返回false
    boolean add(E e);

    //集合移除元素，移除成功，返回true；失败，返回false
    boolean remove(Object o);


    // Bulk Operations---集合块级操作


    //如果此集合包含指定 集合中的所有元素，则返回true。
    boolean containsAll(Collection<?> c);

    //将指定集合中的所有元素添加到此集合（可选操作）。
    boolean addAll(Collection<? extends E> c);

    //删除指定集合中包含的所有此集合的元素（可选操作）。
    boolean removeAll(Collection<?> c);


    /**
     * @param filter --过滤器
     * @return
     * @since 1.8
     */
    //删除满足给定谓词的此集合的所有元素。
    default boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }

    //仅保留此集合中包含在指定集合中的元素（可选操作）。
    boolean retainAll(Collection<?> c);

    void clear();


    // Comparison and hashing--集合的比较和hash值操作

    //将指定的对象与此集合进行比较以获得相等性。
    boolean equals(Object o);

    //设置集合对象的hash值
    int hashCode();

    /**
     * 以下三个接口待定，弄清楚
     */
    /**
     * @return
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 0);
    }

    /**
     * @since 1.8
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * @since 1.8
     */
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
