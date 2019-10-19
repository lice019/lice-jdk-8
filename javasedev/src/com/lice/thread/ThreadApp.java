package com.lice.thread;

/**
 * description: ThreanApp <br>
 * date: 2019/10/6 11:50 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class ThreadApp {
    public static void main(String[] args) {
        //包含main方法，有三条线程
        //保证线程是使用同一个对象资源
        MyThread thread = new MyThread();
        new Thread(thread).start();
        new Thread(thread).start();

    }
}
