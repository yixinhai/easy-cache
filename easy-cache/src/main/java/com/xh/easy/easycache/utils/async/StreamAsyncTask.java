package com.xh.easy.easycache.utils.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

/**
 * 异步任务工具类
 *
 * @param <R> 异步执行方法返回值类型
 * @param <U> 参数2类型
 * @param <T> 参数1类型
 * @author yixinhai
 */
@Slf4j
public class StreamAsyncTask<T, U, R> extends AsyncTask<T, U, R> {

    /**
     * 执行方法任务入参list
     */
    private List<T> baseList;

    /**
     * 指定方法任务入参map
     */
    private Map<T, U> baseMap;


    private StreamAsyncTask() {
    }

    /**
     * 初始化异步任务执行器
     *
     * @param collection 异步任务参数
     * @param R          异步任务返回值class
     * @param <R>        返回值类型
     * @param <T>        入参类型
     * @return 异步任务执行器
     */
    public static <T, U, R> StreamAsyncTask<T, U, R> of(List<T> collection, Class<R> R) {
        StreamAsyncTask<T, U, R> streamAsyncTask = new StreamAsyncTask<>();
        streamAsyncTask.baseList = collection;
        streamAsyncTask.taskType = TASK_R;
        return streamAsyncTask;
    }

    /**
     * 初始化异步任务执行器
     *
     * @param collection 异步任务参数
     * @param R          异步任务返回值class
     * @param <R>        返回值类型
     * @param <T>        入参类型
     * @return 异步任务执行器
     */
    public static <T, U, R> StreamAsyncTask<T, U, R> of(Set<T> collection, Class<R> R) {
        List<T> list = new ArrayList<>(collection);
        return StreamAsyncTask.of(list, R);
    }

    /**
     * 初始化异步任务执行器
     *
     * @param map 异步任务参数
     * @param R          异步任务返回值class
     * @param <R>        返回值类型
     * @param <T>        入参1类型
     * @param <U>        入参2类型
     * @return 异步任务执行器
     */
    public static <T, U, R> StreamAsyncTask<T, U, R> of(Map<T, U> map, Class<R> R) {
        StreamAsyncTask<T, U, R> streamAsyncTask = new StreamAsyncTask<>();
        streamAsyncTask.baseMap = new HashMap<>(map);
        streamAsyncTask.taskType = TASK_R;
        return streamAsyncTask;
    }

    /**
     * 初始化异步任务执行器
     *
     * @param collection 异步任务参数
     * @param <T>        入参类型
     * @return 异步任务执行器
     */
    public static <T, U, R> StreamAsyncTask<T, U, R> of(List<T> collection) {
        StreamAsyncTask<T, U, R> streamAsyncTask = new StreamAsyncTask<>();
        streamAsyncTask.baseList = collection;
        streamAsyncTask.taskType = TASK_VOID;
        return streamAsyncTask;
    }

    /**
     * 初始化异步任务执行器
     *
     * @param collection 异步任务参数
     * @param <T>        入参类型
     * @return 异步任务执行器
     */
    public static <T, U, R> StreamAsyncTask<T, U, R> of(Set<T> collection) {
        collection = Optional.ofNullable(collection).orElse(Collections.emptySet());
        return StreamAsyncTask.of(new ArrayList<>(collection));
    }

    /**
     * 初始化异步任务执行器
     *
     * @param map 异步任务参数
     * @param <T>        入参1类型
     * @param <U>        入参2类型
     * @return 异步任务执行器
     */
    public static <T, U, R> StreamAsyncTask<T, U, R> of(Map<T, U> map) {
        StreamAsyncTask<T, U, R> streamAsyncTask = new StreamAsyncTask<>();
        streamAsyncTask.baseMap = new HashMap<>(map);
        streamAsyncTask.taskType = TASK_VOID;
        return streamAsyncTask;
    }

    /**
     * 无参执行异步任务
     *
     * @param action 异步任务方法
     */
    public StreamAsyncTask<T, U, R> forEachSupplyAsync(Supplier<R> action) {
        if (CollectionUtils.isEmpty(baseList)) {
            return this;
        }
        baseList.forEach(t -> {
            completableFutureTask.add(CompletableFuture.supplyAsync(action, executor));
        });
        return this;
    }

    /**
     * 单入参执行异步任务
     *
     * @param function 异步任务方法
     */
    public StreamAsyncTask<T, U, R> forEachSupplyAsync(Function<? super T, ? extends R> function) {
        if (CollectionUtils.isEmpty(baseList)) {
            return this;
        }
        baseList.forEach(t -> {
            completableFutureTask.add(CompletableFuture.supplyAsync(functionToSupplier(function, t), executor));
        });
        return this;
    }

    /**
     * 多入参执行异步任务
     *
     * @param function 异步任务方法
     */
    public StreamAsyncTask<T, U, R> forEachSupplyAsync(BiFunction<? super T, ? super U, ? extends R> function) {
        if (CollectionUtils.isEmpty(baseMap)) {
            return this;
        }
        baseMap.forEach((t, u) -> {
            completableFutureTask.add(CompletableFuture.supplyAsync(functionToSupplier(function, t, u), executor));
        });
        return this;
    }

    /**
     * 无参无返回值执行异步任务
     *
     * @param action 异步任务方法
     */
    public StreamAsyncTask<T, U, R> forEachRunAsync(Runnable action) {
        if (CollectionUtils.isEmpty(baseList)) {
            return this;
        }
        baseList.forEach(t -> {
            completableFutureVoidTask.add(CompletableFuture.runAsync(action, executor));
        });
        return this;
    }

    /**
     * 单入参无返回值执行异步任务
     *
     * @param action 异步任务方法
     */
    public StreamAsyncTask<T, U, R> forEachRunAsync(Consumer<? super T> action) {
        if (CollectionUtils.isEmpty(baseList)) {
            return this;
        }
        baseList.forEach(t -> {
            completableFutureVoidTask.add(CompletableFuture.runAsync(consumerToRunnable(action, t), executor));
        });
        return this;
    }

    /**
     * 多入参无返回值执行异步任务
     *
     * @return 异步任务方法
     */
    public StreamAsyncTask<T, U, R> forEachRunAsync(BiConsumer<? super T, ? super U> consumer) {
        if (null == baseMap || baseMap.isEmpty()) {
            return this;
        }
        baseMap.forEach((t, u) -> {
            completableFutureVoidTask.add(CompletableFuture.runAsync(consumerToRunnable(consumer, t, u), executor));
        });
        return this;
    }

    /**
     * 执行异步任务，收集异步任务返回值
     *
     * @return 当使用有返回值的异步任务执行方法时返回执行结果
     */
    public List<R> collect() {
        if (CollectionUtils.isEmpty(baseList) && CollectionUtils.isEmpty(baseMap)) {
            return new ArrayList<>();
        }
        List<R> result = new ArrayList<>();
        try {
            result = execTask();
        } catch (ExecutionException | InterruptedException e) {
            log.error("StreamTask_collect error", e);
        }
        return filterNonNull(result);
    }

}
