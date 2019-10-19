package com.lice.thread;

/**
 * description: MyThread <br>
 * date: 2019/10/6 11:37 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
/*
 *java中的实现有两种方式：
 * 1、实现Runnable接口，实现Runnable中的run方法，但此时该类还不是线程类，启动线程还是需要Thread来创建线程。
 *  Thread thread = new Thread(new MyThread()).start();
 *
 * 2、直接继承Thread类，此时该类是一个线程类，直接new，然后调用start方法启动即可。
 *
 * 线程与进程的区别：
 * 一个进程，可以包含多条线程。
 *
 * CPU单核是执行单条线程的，只是在不停的切换，速度很快。
 *
 * 线程是具有不安全性的，因为线程之间如果不做线程通信，线程是独立不通信的。
 *
 * 解决线程安全问题：
 * 1、使用锁机制：锁对象，可以任意的，但必须是同一把锁，
 *      两种锁使用方式：同步代码块
 *                   同步方法（同步方法默认使用该对象this作为锁对象）
 *
 * 2、线程之间的通信，也是依赖同一把锁来进行通信的，锁就好比是通信机构一样。
 *
 * 锁：线程进来执行，到达锁内的代码，必须执行锁内的代码，才释放锁对象（也就是放弃CPU的执行权）
 */
public class MyThread implements Runnable {

    //线程共享资源,两条线程进行同时卖出者10张票
    private int ticket = 10;

    @Override
    public void run() {
        //不能将for进行同步，如果将for进行同步，就会一条线程，直接将票卖完。
        for (int i = 0; i < 10; i++) {
            synchronized (this) {
                if (ticket > 0) {
                    System.out.println(Thread.currentThread().getName() + "票号：" + ticket);
                    ticket--;
                }
            }
        }
    }
}
