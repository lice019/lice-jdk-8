package java.util;

import java.util.function.UnaryOperator;

/**
 * List：list有序集合，并且可重复。派生类有; ArrayList ， LinkedList等
 * List的底层容器是动态数组
 *
 * @since 1.2
 */

public interface List<E> extends Collection<E> {
    // Query Operations---查询操作

    int size();

    boolean isEmpty();


    boolean contains(Object o);


    Iterator<E> iterator();


    Object[] toArray();


    <T> T[] toArray(T[] a);


    // Modification Operations --修改操作


    boolean add(E e);

    boolean remove(Object o);


    // Bulk Modification Operations
    boolean containsAll(Collection<?> c);


    boolean addAll(Collection<? extends E> c);


    boolean addAll(int index, Collection<? extends E> c);


    boolean removeAll(Collection<?> c);


    boolean retainAll(Collection<?> c);

    /**
     * 将该列表的每个元素替换为将该运算符应用于该元素的结果。
     * @since 1.8
     */
    default void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }

    /**
     * 使用随附的 Comparator排序此列表来比较元素。
     * @since 1.8
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }


    //清空集合
    void clear();


    // Comparison and hashing

    boolean equals(Object o);


    int hashCode();


    // Positional Access Operations --位置访问操作


    //返回下标index的元素，注意下标越界异常
    E get(int index);

    //用指定的元素（可选操作）替换此列表中指定位置的元素。注意下标越界异常
    E set(int index, E element);

    //将指定的元素插入此列表中的指定位置（可选操作）。 index之后的元素，往后偏移一个一个下标
    void add(int index, E element);

    //移除下标为index的元素
    E remove(int index);


    // Search Operations --搜索操作

    //返回元素o的下标，注意下标越界异常
    int indexOf(Object o);

    //返回集合中与o元素相同的最后一个下标
    int lastIndexOf(Object o);


    // List Iterators
    //返回列表中的列表迭代器（按适当的顺序）。
    ListIterator<E> listIterator();

    //从列表中的指定位置开始，返回列表中的元素（按正确顺序）的列表迭代器。
    ListIterator<E> listIterator(int index);

    // View
    //返回从下标为fromIndex到toIndex下标的子集合
    List<E> subList(int fromIndex, int toIndex);

    /**
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
}
