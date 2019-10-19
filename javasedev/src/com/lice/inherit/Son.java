package com.lice.inherit;

import java.lang.reflect.Constructor;

/**
 * description: Son <br>
 * date: 2019/8/28 17:26 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class Son extends Parent {
    private String sName;

    public Son() {
        System.out.println("Son的无参构造器执行了...");
    }

    public Son(String sName) {
        System.out.println("Son的有参构造器执行了...");
        this.sName = sName;
    }

    public static void main(String[] args) throws Exception {
        //Son son = new Son();
        Class clazz = Class.forName("com.lice.inherit.Parent");
        Parent o = (Parent) clazz.newInstance();
        Constructor c = clazz.getConstructor(String.class);
        Parent p = (Parent) c.newInstance("huahg");
    }
}
