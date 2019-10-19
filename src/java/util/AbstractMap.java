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
        Iterator<Entry<K, V>> i = entrySet().iterator();
        //由于HashMap是允许null为key-value的，所以判断是否value为null
        if (value == null) {
            //遍历map中元素
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (e.getValue() == null)
                    //如果有为null的value，直接返回true
                    return true;
            }
        } else {
            //如果value不是null，则判断相应的值
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (value.equals(e.getValue()))
                    return true;
            }
        }
        return false;
    }

    //判断map是否包含该key
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

    //添加key和value
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    //删除key
    public V remove(Object key) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        Entry<K, V> correctEntry = null;
        if (key == null) {
            while (correctEntry == null && i.hasNext()) {
                Entry<K, V> e = i.next();
                if (e.getKey() == null)
                    correctEntry = e;
            }
        } else {
            while (correctEntry == null && i.hasNext()) {
                Entry<K, V> e = i.next();
                if (key.equals(e.getKey()))
                    correctEntry = e;
            }
        }

        V oldValue = null;
        if (correctEntry != null) {
            oldValue = correctEntry.getValue();
            i.remove();
        }
        return oldValue;
    }


    // Bulk Operations----块级操作


    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    //清空map中的所有Entry对象
    public void clear() {
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


    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
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


    public int hashCode() {
        int h = 0;
        Iterator<Entry<K, V>> i = entrySet().iterator();
        while (i.hasNext())
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
