package com.axiang.cropimagedemo.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 * Created by 邱翔威 on 2022/4/11
 */
public class ThreadPoolUtil {

    // 线程池核心线程数
    private static final int CORE_POOL_SIZE = 2;
    // 线程池最大线程数
    private static final int MAX_POOL_SIZE = 100;
    // 额外线程空状态生存时间
    private static final int KEEP_ALIVE_TIME = 10000;
    // 阻塞队列，当核心线程都被占用，且阻塞队列已满的情况下，才会开启额外线程
    private static final BlockingQueue<Runnable> sWorkQueue = new ArrayBlockingQueue<>(10);
    // 线程池
    private static final ThreadPoolExecutor sThreadPool;

    private ThreadPoolUtil() {
    }

    // 线程工厂
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        private final AtomicInteger mAtomicInteger = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadPool thread:" + mAtomicInteger.getAndIncrement());
        }
    };

    static {
        sThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, sWorkQueue, sThreadFactory);
    }

    public static void execute(Runnable runnable) {
        sThreadPool.execute(runnable);
    }

    public static void execute(FutureTask<?> futureTask) {
        sThreadPool.execute(futureTask);
    }

    public static void cancel(FutureTask<?> futureTask) {
        futureTask.cancel(true);
    }
}
