package com.lice.map.hashMap;

import com.lice.map.AbstractMap;
import com.lice.map.Map;

import java.io.Serializable;
import java.util.Objects;

/**
 * description: HashMap <br>
 * date: 2019/10/4 22:22 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    //默认初始化HashMap的空间大小
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    //最大容量
    static final int MAXIMUM_CAPACITY = 1 << 30;

    //map中的元素个数，实际是Node节点的个数
    private int size = 0;

    //数组存储
    private Node<K, V>[] table;

    public HashMap() {

    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V put(K key, V value) {
        putVal(key.hashCode(), key, value);
        return value;
    }

    public V get(Object key) {
        Node<K, V> node;
        for (int i = 0; i < table.length; i++) {
            if (table[i].getKey() == key) {
                node = table[i];
                return node.getValue();
            }
        }
        return null;
    }

    private final V putVal(int hash, K key, V value) {
        Node<K, V>[] tab = new Node[size() + 1];
        Node<K, V> p;
//        int i;
        if (table == null || table.length == 0) {
            tab[0] = newNode(hash, key, value, null);
            table = tab;
            size++;
            return null;
        }
        if (table != null || table.length != 0) {
            for (int i = 0; i < table.length; i++) {
                tab[i] = table[i];
            }
            tab[tab.length - 1] = newNode(hash, key, value, null);
            table = tab;
            size++;
            return table[table.length - 2].value;
        }
        return null;
    }

    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }

    static class Node<K, V> implements Map.Entry<K, V> {

        int hash;
        K key;
        V value;

        Node<K, V> next;//下一个节点

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<K, V>) o;
                if (Objects.equals(key, entry.getKey()) && Objects.equals(value, entry.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }
}
