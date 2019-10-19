package java.util;

/**
 * 哈希表和链表实现了Set接口，具有可预测的迭代次序。 这种实现不同于HashSet，它维持于所有条目的运行双向链表。
 * LinkedHashSet底层容器还是HashMap，只是LinkedHashSet是使用继承父类的HashSet容器。
 * LinkedHashSet没做什么处理，都是使用HashMap中Node的链表来实现链表的数据结构
 *
 * @param <E>
 */
public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2851667679971038690L;


    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }


    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity, .75f, true);
    }


    public LinkedHashSet() {
        super(16, .75f, true);
    }


    public LinkedHashSet(Collection<? extends E> c) {
        super(Math.max(2 * c.size(), 11), .75f, true);
        addAll(c);
    }


    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.ORDERED);
    }
}
