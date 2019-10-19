package com.lice.inherit;

/**
 * description: Parent <br>
 * date: 2019/8/28 17:24 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class Parent {

    private String pName;

    public Parent() {
        System.out.println("parent的无参构造器执行了.......");
    }

    public Parent(String pName) {
        System.out.println("parent的有参构造器执行了");
        this.pName = pName;
    }
}
