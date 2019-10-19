package com.lice.thread;

/**
 * description: SynchronizedMethod线程安全机制的同步方法 <br>
 * date: 2019/10/6 12:00 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class SynchronizedMethod implements Runnable {

    //共享票资源
    private int ticket = 10;

    //同步方法，java默认使用this为锁对象
    //将需要同步的方法抽出来
    private synchronized void sellTicket() {
        if (ticket > 0) {
            System.out.println(Thread.currentThread().getName() + "票号" + ticket);
            ticket--;
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            //调用同步方法
            sellTicket();
        }

    }

    public static void main(String[] args) {
        SynchronizedMethod thread = new SynchronizedMethod();
        new Thread(thread).start();
        new Thread(thread).start();

    }
}
