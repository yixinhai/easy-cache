package com.xh.easy.easycache.base;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

public class TimeWindowCounter {

    /**
     * 默认队列最大容量
     */
    private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;

    /**
     * 窗口大小
     * 每隔 windowSizeMs 毫秒执行一次事件队列清理
     */
    private final long windowSizeMs;

    /**
     * 队列最大容量
     */
    private final int maxQueueSize;

    /**
     * 事件队列
     */
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Event>> eventQueues;

    /**
     * 队列清理定时任务
     */
    private final ScheduledExecutorService cleaner;

    /**
     * 构造函数
     * 
     * @param windowSizeMs
     *     窗口大小
     */
    public TimeWindowCounter(long windowSizeMs) {
        this(windowSizeMs, DEFAULT_MAX_QUEUE_SIZE);
    }

    /**
     * 构造函数
     * 
     * @param windowSizeMs
     *     窗口大小
     * @param maxQueueSize
     *     队列最大容量
     */
    public TimeWindowCounter(long windowSizeMs, int maxQueueSize) {
        this.windowSizeMs = windowSizeMs;
        this.eventQueues = new ConcurrentHashMap<>();
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.maxQueueSize = maxQueueSize;

        // 定期清理过期事件
        cleaner.scheduleAtFixedRate(this::cleanExpiredEvents, windowSizeMs, windowSizeMs, TimeUnit.MILLISECONDS);
    }

    public void increment(String operationId) {
        ConcurrentLinkedQueue<Event> queue = eventQueues.get(operationId);
        if (queue == null || queue.size() < maxQueueSize) {
            eventQueues.computeIfAbsent(operationId, k -> new ConcurrentLinkedQueue<>())
                    .add(new Event(Instant.now()));
        }
    }

    public int getCount(String operationId) {
        ConcurrentLinkedQueue<Event> queue = eventQueues.get(operationId);
        if (queue == null) {
            return 0;
        }

        Instant cutoff = Instant.now().minusMillis(windowSizeMs);
        // 使用原子计数器确保统计过程中的线程安全
        AtomicInteger count = new AtomicInteger(0);
        queue.forEach(event -> {
            if (!event.timestamp.isBefore(cutoff)) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    private void cleanExpiredEvents() {
        System.out.println("Cleaning expired events...");
        Instant cutoff = Instant.now().minusMillis(windowSizeMs);
        List<String> emptyQueues = new ArrayList<>();
        
        // 第一阶段：只读遍历，收集需要删除的key
        eventQueues.forEach((operationId, queue) -> {
            while (!queue.isEmpty() && queue.peek().timestamp.isBefore(cutoff)) {
                queue.poll();  // 只修改queue内容，不修改Map结构
            }
            if (queue.isEmpty()) {
                emptyQueues.add(operationId);  // 只是记录，不删除
            }
        });
        
        // 第二阶段：批量删除，此时已经结束遍历
        emptyQueues.forEach(eventQueues::remove);
    }

    public void shutdown() {
        cleaner.shutdown();
        try {
            if (!cleaner.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                cleaner.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleaner.shutdownNow();
        }
    }

    private static class Event {
        final Instant timestamp;

        Event(Instant timestamp) {
            this.timestamp = timestamp;
        }
    }

    // 示例用法
    public static void main(String[] args) throws InterruptedException {
        TimeWindowCounter counter = new TimeWindowCounter(5000); // 5秒窗口

        // 模拟操作触发
        counter.increment("login");
        Thread.sleep(1000);
        counter.increment("login");
        Thread.sleep(1000);
        counter.increment("login");

        System.out.println("Login count in 5s: " + counter.getCount("login")); // 应输出3

        Thread.sleep(4000); // 等待总时间超过5秒
        System.out.println("Login count after 5s: " + counter.getCount("login")); // 应输出0

        counter.shutdown();
    }
}