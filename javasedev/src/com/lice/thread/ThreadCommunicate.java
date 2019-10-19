package com.lice.thread;

/**
 * description: ThreadCommunicate 线程间的通信<br>
 * date: 2019/10/6 12:08 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
/*
 *线程间的通信三大要素：锁对象，wait，notify,notifyAll
 *
 * 1、锁：必须使用同一把锁
 * 2、wait：线程等待方法，当前线程进行等待，可以设置等待时间
 * 3、notify：线程唤醒机制，唤醒持有同一把锁的另一线程
 * 4、notifyAll：唤醒持有同一锁的其他所有线程
 */
public class ThreadCommunicate {

    public static void main(String[] args) {

        new MyThread1().start();
        new MyThread2().start();
    }

}

/*
 *需要：两天线程，同时进行输出，交替输出1,2
 */
//第一条线程
class MyThread1 extends Thread {
    @Override
    public void run() {
        //输出10次
        for (int i = 0; i < 10; i++) {
            synchronized (Lock.lock) {
                System.out.println(Thread.currentThread().getName() +":"+ 1);
                //先唤醒对方，在进行等待；如果先等待，就唤醒不了对方法了，这个顺序需要注意
                Lock.lock.notify();
                try {
                    Lock.lock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }
}

class MyThread2 extends Thread {
    @Override
    public void run() {
        //输出10次
        for (int i = 0; i < 10; i++) {
            synchronized (Lock.lock) {
                System.out.println(Thread.currentThread().getName() +":"+ 2);
                //先唤醒对方，在进行等待；如果先等待，就唤醒不了对方法了，这个顺序需要注意
                Lock.lock.notify();
                try {
                    Lock.lock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }
}

//锁类，里面的Object对象为锁
class Lock {

    public static Object lock = new Object();
}