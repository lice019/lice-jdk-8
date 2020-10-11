package com.lice;

import com.lice.inherit.Parent;

/**
 * description: Demo 用于测试快速测试各种功能和计算数<br>
 * date: 2019/10/23 1:40 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class Demo {
    public static void main(String[] args) {

        int a = 10, b = 20;
        if (a < b) {
            swap(a,b);
        }
        System.out.println("a="+a+";b="+b);
    }

    public static void  swap(int a, int b) {
        int temp;
        temp = a;
        a = b;
        b = temp;
    }
}
