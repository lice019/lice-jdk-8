package java.util;

/**
 * AbstractMap:为map子类的模板类，AbstractMap中没有定义相应的容器，容器有相应的子类来定义，这里只定义相应的操作方法
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractMap<K, V> implements Map<K, V> {

    //AbstractMap构造器，抽象类时不能通过构造器来new实例，这个只能给子类来调用，而且使用了protected来修饰
    protected AbstractMap() {
    }

    // Query Operations

    //
    public int size() {
        return entrySet().size();
    }

    //size为0即返回true，map中元素为空
    public boolean isEmpty() {
        return size() == 0;
    }

    //判断map中是否有该value值
    public boolean containsValue(Object value) {

        /*
         *entrySet()在AbstractMap中是没有实现的，让子类去实现。
         *entrySet()是获取所有的Entry对象，每个Entry存储在Set集合中返回
         * 这里通过获取存储了Entry对象的Set集合去获取迭代器
         * 迭代器可以遍历出Set中的元素
         */
        Iterator<Entry<K, V>> i = entrySet().iterator();

        //由于HashMap是允许null为key-value的，所以判断是否value为null
        //而key是唯一的，value不是唯一的

        /*
         *先分两种情况来判断：
         * 1、value==null
         * 2、value!=null
         *
         */
        //第一种情况
        if (value == null) {
            //遍历Set集合中的Entry对象
            while (i.hasNext()) {
                //set集合中有元素，取出Entry对象
                Entry<K, V> e = i.next();
                //获取Entry对象的属性value，如果value==null
                //返回true，说明Map中的确有value为null元素
                if (e.getValue() == null)
                    //如果有为null的value，直接返回true
                    return true;
            }
            //第二种情况：value!=null
        } else {
            //如果value不是null，也是通过迭代器取出Set集合中的Entry对象
            while (i.hasNext()) {
                Entry<K, V> e = i.next();

                /*
                 *判断Entry对象中的value值是否相等
                 * 此处是使用equals()方法是判断对象是否相等
                 * 基本类型数据应该会进行装箱成对象类型
                 */
                if (value.equals(e.getValue()))
                    return true;
            }
        }
        //没有返回false
        return false;
    }

    /*
     *判断Map中是否存在key
     * 实际是判断Set集合中的Entry对象是否存在key，与上面的判断value的做法一样
     */
    public boolean containsKey(Object key) {
        //获取Entry中的key迭代器
        Iterator<Map.Entry<K, V>> i = entrySet().iterator();
        if (key == null) {
            //遍历
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (e.getKey() == null)
                    return true;
            }
        } else {
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (key.equals(e.getKey()))
                    return true;
            }
        }
        return false;
    }

    //根据key获取value
    public V get(Object key) {
        //获取Entry对象的集合
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (key == null) {
            //遍历map中所有的Entry对象
            while (i.hasNext()) {
                //获取下一个Entry对象
                Entry<K, V> e = i.next();
                //比较Entry对象中key，如果为该key，则返回相应的value
                if (e.getKey() == null)
                    return e.getValue();
            }
        } else {
            //如果key不为null
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (key.equals(e.getKey()))
                    return e.getValue();
            }
        }
        return null;
    }


    // Modification Operations

    /*
     *Map中put方法，留给子类实现
     */
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    //删除key，并返回key
    public V remove(Object key) {
        //获取set集合中的entry对象
        Iterator<Entry<K, V>> i = entrySet().iterator();
        //声明一个变，用于临时存放要删除key的entry对象
        Entry<K, V> correctEntry = null;

        /*
         *分两种情况判断：
         * 1、key==null
         * 2、key！=null
         *
         * 注意：map中的key是唯一的，也就是一个map集合中只能有一个key是null。
         */

        if (key == null) {
            while (correctEntry == null && i.hasNext()) {
                Entry<K, V> e = i.next();
                //如entry对象中key属性值为null
                if (e.getKey() == null)
                    //将含有属性含有null的entry对象赋给临时变量correctEntry
                    correctEntry = e;
            }
        } else {
            //第二种情况
            while (correctEntry == null && i.hasNext()) {
                Entry<K, V> e = i.next();
                if (key.equals(e.getKey()))
                    correctEntry = e;
            }
        }

        //临时存储key为null的entry属性value的变量
        V oldValue = null;
        if (correctEntry != null) {
            //如果是有这个key的entry对象，则取出相应的entry的value，赋给oldValue临时变量
            oldValue = correctEntry.getValue();
            //将Set集合中的entry对象删除
            i.remove();
        }
        //如有要删除的key，则返回相应的value；如果没有则返回null
        return oldValue;
    }


    // Bulk Operations----块级操作


    //将一个map集合put到该map中，块级操作
    public void putAll(Map<? extends K, ? extends V> m) {
        //循环出m的entry对象，调用map的单个put方法，一个一个put到该map中
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    //清空map中的所有Entry对象
    public void clear() {
        //清空map中entry对象
        entrySet().clear();
    }


    // Views

    //key的Set集合
    transient Set<K> keySet;
    //value集合
    transient Collection<V> values;


    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            //匿名类来实现接口方法
            ks = new AbstractSet<K>() {
                public Iterator<K> iterator() {
                    return new Iterator<K>() {

                        //keySet实际还是从entry对象取出key和value的
                        private Iterator<Entry<K, V>> i = entrySet().iterator();

                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        public K next() {
                            return i.next().getKey();
                        }

                        public void remove() {
                            i.remove();
                        }
                    };
                }

                public int size() {
                    //AbstractMap.this语法是指定外围的AbstractMap对象
                    return AbstractMap.this.size();
                }

                public boolean isEmpty() {
                    return AbstractMap.this.isEmpty();
                }

                public void clear() {
                    AbstractMap.this.clear();
                }

                public boolean contains(Object k) {
                    return AbstractMap.this.containsKey(k);
                }
            };
            keySet = ks;
        }
        return ks;
    }


    public Collection<V> values() {
        Collection<V> vals = values;
        if (vals == null) {
            vals = new AbstractCollection<V>() {
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        private Iterator<Entry<K, V>> i = entrySet().iterator();

                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        public V next() {
                            return i.next().getValue();
                        }

                        public void remove() {
                            i.remove();
                        }
                    };
                }

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean isEmpty() {
                    return AbstractMap.this.isEmpty();
                }

                public void clear() {
                    AbstractMap.this.clear();
                }

                public boolean contains(Object v) {
                    return AbstractMap.this.containsValue(v);
                }
            };
            values = vals;
        }
        return vals;
    }

    //获取Map中Entry对象集合
    public abstract Set<Entry<K, V>> entrySet();


    // Comparison and hashing


    /*
     *这里的equals()方法，全等的判断，判断的是堆内存中的对象是否相等
     */
    public boolean equals(Object o) {

        //如果是该对象直接返回true
        if (o == this)
            return true;

        //如果不是Map的实现类或Map实现类的子类则返回false
        if (!(o instanceof Map))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
        //元素个数不等也不是
        if (m.size() != size())
            return false;

        try {
            Iterator<Entry<K, V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }


    //对象的hash值
    public int hashCode() {
        int h = 0;
        Iterator<Entry<K, V>> i = entrySet().iterator();
        while (i.hasNext())
            // h = h + i.next().hashCode();
            /*
             *Map中每一个entry对象的hash值相加则等于该对象的hash值
             */
            h += i.next().hashCode();
        return h;
    }


    public String toString() {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    //克隆
    protected Object clone() throws CloneNotSupportedException {
        AbstractMap<?, ?> result = (AbstractMap<?, ?>) super.clone();
        result.keySet = null;
        result.values = null;
        return result;
    }


    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    // Implementation Note: SimpleEntry and SimpleImmutableEntry
    // are distinct unrelated classes, even though they share
    // some code. Since you can't add or subtract final-ness
    // of a field in a subclass, they can't share representations,
    // and the amount of duplicated code is too small to warrant
    // exposing a common abstract class.

    /*
     *实现注意:SimpleEntry和SimpleImmutableEntry是不同的、不相关的类，尽管它们共享一些代码。
     * 由于您不能在子类中添加或减去字段的结束度，所以它们不能共享表示形式，而且重复代码的数量太少，
     * 不足以保证公开公共抽象类。
     *
     */


    //简单的Entry对象，SimpleEntry实现了Map中Entry接口
    public static class SimpleEntry<K, V>
            implements Entry<K, V>, java.io.Serializable {
        //反序列化id
        private static final long serialVersionUID = -8499721149061103585L;

        //key
        private final K key;
        //value
        private V value;

        //构造器，实例化对象时，传入key和value
        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        //传入一个Entry对象来实例化SimpleEntry
        public SimpleEntry(Entry<? extends K, ? extends V> entry) {
            //获取传入Entry对象的相应key和value
            this.key = entry.getKey();
            this.value = entry.getValue();
        }


        public K getKey() {
            return key;
        }


        public V getValue() {
            return value;
        }

        //设置Entry对象的value
        public V setValue(V value) {
            //改变value时，将被改的value返回
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }


        //判断entry对象的key和value是否相等
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }


        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^
                    (value == null ? 0 : value.hashCode());
        }


        public String toString() {
            return key + "=" + value;
        }

    }


    public static class SimpleImmutableEntry<K, V>
            implements Entry<K, V>, java.io.Serializable {
        private static final long serialVersionUID = 7138329143949025153L;

        private final K key;
        private final V value;

        /**
         * Creates an entry representing a mapping from the specified
         * key to the specified value.
         *
         * @param key   the key represented by this entry
         * @param value the value represented by this entry
         */
        public SimpleImmutableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Creates an entry representing the same mapping as the
         * specified entry.
         *
         * @param entry the entry to copy
         */
        public SimpleImmutableEntry(Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }


        public K getKey() {
            return key;
        }


        public V getValue() {
            return value;
        }


        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }


        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }


        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^
                    (value == null ? 0 : value.hashCode());
        }


        public String toString() {
            return key + "=" + value;
        }

    }

}
