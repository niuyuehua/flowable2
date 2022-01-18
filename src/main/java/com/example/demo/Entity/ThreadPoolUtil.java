package com.example.demo.Entity;


import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4, 20,
            TimeUnit.MILLISECONDS, (BlockingQueue)new LinkedBlockingQueue<>());
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }
}


