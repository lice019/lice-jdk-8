package java.util;

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.io.IOException;

/**
 * LinkedHashMap：链表和hash结合的Map集合，链表为双向链表
 * 实际上LinkedHashMap是继承了HashMap，是HashMap的子类。
 *
 * @param <K>
 * @param <V>
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> implements Map<K, V> {


    //LinkedHashMap使用通过扩展父类HashMap中Node类来实现双端双向链表的
    static class Entry<K, V> extends HashMap.Node<K, V> {
        /*
         * 声明元素的前一个元素的指针，和后一个元素的指针
         */
        Entry<K, V> before, after;

        /*
         * 将两个节点转成Entry对象
         *
         * @param hash  第一个Node的hash值
         * @param key   第一个节点的Key值
         * @param value 第一个节点的Value值
         * @param next  第二个节点
         */
        //直接使用HashMap中Node对象来建立Entry对象
        Entry(int hash, K key, V value, Node<K, V> next) {
            super(hash, key, value, next);
        }
    }

    private static final long serialVersionUID = 3801124242820219131L;

    //头结点
    transient LinkedHashMap.Entry<K, V> head;

    //尾结点
    transient LinkedHashMap.Entry<K, V> tail;

    //标记集合元素是否存取顺序
    final boolean accessOrder;

    // internal utilities

    // link at the end of list
    //链表的尾部进行节点的添加
    private void linkNodeLast(LinkedHashMap.Entry<K, V> p) {
        LinkedHashMap.Entry<K, V> last = tail;
        //尾结点和头结点都为p
        tail = p;
        if (last == null)
            head = p;
        else {
            //p的上一个节点为last
            p.before = last;
            //
            last.after = p;
        }
    }

    // apply src's links to dst
    //反转链表结构
    private void transferLinks(LinkedHashMap.Entry<K, V> src,
                               LinkedHashMap.Entry<K, V> dst) {
        LinkedHashMap.Entry<K, V> b = dst.before = src.before;
        LinkedHashMap.Entry<K, V> a = dst.after = src.after;
        if (b == null)
            head = dst;
        else
            b.after = dst;
        if (a == null)
            tail = dst;
        else
            a.before = dst;
    }

    // overrides of HashMap hook methods

    //初始化LinkedHashMap中参数
    void reinitialize() {
        super.reinitialize();
        //头结点和尾结点至为null
        head = tail = null;
    }

    //创建一个新的node节点
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> e) {
        //创建一个LinkedHashMap中的Entry对象
        LinkedHashMap.Entry<K, V> p =
                new LinkedHashMap.Entry<K, V>(hash, key, value, e);

        //尾部添加节点
        linkNodeLast(p);
        //尾部添加后，返回
        return p;
    }

    Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        LinkedHashMap.Entry<K, V> q = (LinkedHashMap.Entry<K, V>) p;
        LinkedHashMap.Entry<K, V> t =
                new LinkedHashMap.Entry<K, V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    //初始化一个TreeNode
    TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        TreeNode<K, V> p = new TreeNode<K, V>(hash, key, value, next);
        linkNodeLast(p);
        return p;
    }

    TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        LinkedHashMap.Entry<K, V> q = (LinkedHashMap.Entry<K, V>) p;
        TreeNode<K, V> t = new TreeNode<K, V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    void afterNodeRemoval(Node<K, V> e) { // unlink
        LinkedHashMap.Entry<K, V> p =
                (LinkedHashMap.Entry<K, V>) e, b = p.before, a = p.after;
        p.before = p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a == null)
            tail = b;
        else
            a.before = b;
    }

    void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K, V> first;
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            removeNode(hash(key), key, null, false, true);
        }
    }

    void afterNodeAccess(Node<K, V> e) { // move node to last
        LinkedHashMap.Entry<K, V> last;
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K, V> p =
                    (LinkedHashMap.Entry<K, V>) e, b = p.before, a = p.after;
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }

    //序列化
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
            s.writeObject(e.key);
            s.writeObject(e.value);
        }
    }


    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }


    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }


    public LinkedHashMap() {
        super();
        accessOrder = false;
    }


    public LinkedHashMap(Map<? extends K, ? extends V> m) {
        super();
        accessOrder = false;
        putMapEntries(m, false);
    }


    public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }


    public boolean containsValue(Object value) {
        for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
            V v = e.value;
            if (v == value || (value != null && value.equals(v)))
                return true;
        }
        return false;
    }


    public V get(Object key) {
        Node<K, V> e;
        if ((e = getNode(hash(key), key)) == null)
            return null;
        if (accessOrder)
            afterNodeAccess(e);
        return e.value;
    }

    public V getOrDefault(Object key, V defaultValue) {
        Node<K, V> e;
        if ((e = getNode(hash(key), key)) == null)
            return defaultValue;
        if (accessOrder)
            afterNodeAccess(e);
        return e.value;
    }

    public void clear() {
        super.clear();
        head = tail = null;
    }


    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return false;
    }


    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new LinkedKeySet();
            keySet = ks;
        }
        return ks;
    }

    final class LinkedKeySet extends AbstractSet<K> {
        public final int size() {
            return size;
        }

        public final void clear() {
            LinkedHashMap.this.clear();
        }

        public final Iterator<K> iterator() {
            return new LinkedKeyIterator();
        }

        public final boolean contains(Object o) {
            return containsKey(o);
        }

        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }

        public final Spliterator<K> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED |
                    Spliterator.ORDERED |
                    Spliterator.DISTINCT);
        }

        public final void forEach(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            int mc = modCount;
            for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
                action.accept(e.key);
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }


    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new LinkedValues();
            values = vs;
        }
        return vs;
    }

    final class LinkedValues extends AbstractCollection<V> {
        public final int size() {
            return size;
        }

        public final void clear() {
            LinkedHashMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new LinkedValueIterator();
        }

        public final boolean contains(Object o) {
            return containsValue(o);
        }

        public final Spliterator<V> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED |
                    Spliterator.ORDERED);
        }

        public final void forEach(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            int mc = modCount;
            for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
                action.accept(e.value);
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }


    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es;
        return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
    }

    final class LinkedEntrySet extends AbstractSet<Map.Entry<K, V>> {
        public final int size() {
            return size;
        }

        public final void clear() {
            LinkedHashMap.this.clear();
        }

        public final Iterator<Map.Entry<K, V>> iterator() {
            return new LinkedEntryIterator();
        }

        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object key = e.getKey();
            Node<K, V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }

        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }

        public final Spliterator<Map.Entry<K, V>> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED |
                    Spliterator.ORDERED |
                    Spliterator.DISTINCT);
        }

        public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            int mc = modCount;
            for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
                action.accept(e);
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    // Map overrides

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
            action.accept(e.key, e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after)
            e.value = function.apply(e.key, e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }

    // Iterators

    abstract class LinkedHashIterator {
        LinkedHashMap.Entry<K, V> next;
        LinkedHashMap.Entry<K, V> current;
        int expectedModCount;

        LinkedHashIterator() {
            next = head;
            expectedModCount = modCount;
            current = null;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final LinkedHashMap.Entry<K, V> nextNode() {
            LinkedHashMap.Entry<K, V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            current = e;
            next = e.after;
            return e;
        }

        public final void remove() {
            Node<K, V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class LinkedKeyIterator extends LinkedHashIterator
            implements Iterator<K> {
        public final K next() {
            return nextNode().getKey();
        }
    }

    final class LinkedValueIterator extends LinkedHashIterator
            implements Iterator<V> {
        public final V next() {
            return nextNode().value;
        }
    }

    final class LinkedEntryIterator extends LinkedHashIterator
            implements Iterator<Map.Entry<K, V>> {
        public final Map.Entry<K, V> next() {
            return nextNode();
        }
    }


}
