package com.lice.map;

import java.util.Collection;
import java.util.Set;

/**
 * description: 模拟Map接口 <br>
 * date: 2019/10/4 22:04 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public interface Map<K, V> {

    //查询操作
    int size();

    boolean isEmpty();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    V get(Object key);

    //修改操作

    V put(K key, V value);

    V remove(Object key);

    //块级操作

    void putAll(Map<? extends K, ? extends V> m);

    void clear();


    //view
    Set<K> keySet();

    Collection<V> values();

    Set<Map.Entry<K, V>> entrySet();


    //Entry内部接口

    interface Entry<K, V> {

        K getKey();

        V getValue();

        V setValue(V newValue);

        boolean equals(Object o);

        //设置Node的hashCode
        int hashCode();

    }


}
