package com.lice.thread.pool;

import java.util.LinkedList;

/**
 * description: SimpleThreadPool 简单线程池<br>
 * date: 2019/11/24 23:05 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
/*
 * 1、任务队列
 * 2、拒绝策略（抛出异常，直接丢弃，阻塞，临时队列）
 * 3、init()初始化
 * 4、active线程存活
 * 5、max最大数
 * 6、min最小数
 */
public class SimpleThreadPool {

    private final int size;

    private final static int DEFAULT_SIZE = 10;

    //任务队列
    private final static LinkedList<Runnable> TASK_QUEUE = new LinkedList<>();

    public SimpleThreadPool() {
        this(DEFAULT_SIZE);
    }

    public SimpleThreadPool(int size) {
        this.size = size;
        init();

    }

    //初始化
    private void init() {

    }

    //线程状态
    private enum TaskState {
        FREE, RUNNING, BLOCK, DEAD;
    }

    //工作任务
    private static class WorkerTask extends Thread {
        private volatile TaskState taskState = TaskState.FREE;


        public WorkerTask(ThreadGroup threadGroup, String name) {
            super(threadGroup, name);
        }

        public TaskState getTaskState() {
            return this.taskState;
        }

        //重写run方法
        public void run() {
            //循环没有死亡的线程，并执行线程的任务
            while (this.taskState!=TaskState.DEAD){

            }
        }

        //关闭线程（线程死亡）
        public void close() {
            this.taskState = TaskState.DEAD;
        }
    }


}
