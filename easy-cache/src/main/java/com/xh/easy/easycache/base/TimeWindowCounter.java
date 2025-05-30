package com.xh.easy.easycache.base;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeWindowCounter {

    private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
    private final long windowSizeMs;
    private final int maxQueueSize;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Event>> eventQueues;
    private final ScheduledExecutorService cleaner;

    public TimeWindowCounter(long windowSizeMs) {
        this(windowSizeMs, DEFAULT_MAX_QUEUE_SIZE);
    }

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
        if (queue.size() < maxQueueSize) {
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
        eventQueues.forEach((operationId, queue) -> {
            while (!queue.isEmpty() && queue.peek().timestamp.isBefore(cutoff)) {
                queue.poll();
            }
            // 如果队列空了，移除该操作ID对应的队列
            if (queue.isEmpty()) {
                eventQueues.remove(operationId);
            }
        });
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