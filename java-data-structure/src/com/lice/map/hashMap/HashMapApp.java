package com.lice.map.hashMap;

/**
 * description: HashMapApp <br>
 * date: 2019/10/4 23:15 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class HashMapApp {
    public static void main(String[] args) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("1", "one");
        hashMap.put("2", "two");
        hashMap.put("3", new String("1234"));
        System.out.println(hashMap.isEmpty());
        System.out.println(hashMap.size());
        System.out.println(hashMap.get("1"));
        System.out.println(hashMap.get("2"));
        System.out.println(hashMap.get("3"));
    }
}
