package com.xh.easy.easycache.async;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

/**
 * 异步任务执行器
 *
 * @param <T> 入参1类型
 * @param <U> 入参2类型
 * @param <R> 返回值类型
 * @author yixinhai
 */
@Slf4j
public class FunctionAsyncTask<T, U, R> extends AsyncTask<T, U, R> {

    private FunctionAsyncTask(){
    }

    /**
     * 初始化异步任务执行器
     *
     * @param <R>        返回值类型
     * @param <U>        入参2类型
     * @param <T>        入参1类型
     * @return 异步任务执行器
     */
    public static <T, U, R> FunctionAsyncTask<T, U, R> getSupplyAsyncInstance() {
        FunctionAsyncTask<T, U, R> instance = new FunctionAsyncTask<>();
        instance.taskType = TASK_R;
        return instance;
    }

    /**
     * 初始化异步任务执行器
     *
     * @param <R>        返回值类型
     * @param <U>        入参2类型
     * @param <T>        入参1类型
     * @return 异步任务执行器
     */
    public static <T, U, R> FunctionAsyncTask<T, U, R> getRunAsyncInstance() {
        FunctionAsyncTask<T, U, R> instance = new FunctionAsyncTask<>();
        instance.taskType = TASK_VOID;
        return instance;
    }

    public FunctionAsyncTask<T, U, R> addTask(BiFunction<? super T, ? super U, ? extends R> function, T t, U u) {
        completableFutureTask.add(CompletableFuture.supplyAsync(functionToSupplier(function, t, u), executor));
        return this;
    }

    public FunctionAsyncTask<T, U, R> addTask(Function<? super T, ? extends R> function, T t) {
        completableFutureTask.add(CompletableFuture.supplyAsync(functionToSupplier(function, t), executor));
        return this;
    }

    public FunctionAsyncTask<T, U, R> addTask(Supplier<R> supplier) {
        completableFutureTask.add(CompletableFuture.supplyAsync(supplier, executor));
        return this;
    }

    public FunctionAsyncTask<T, U, R> addTask(Consumer<? super T> consumer, T t) {
        completableFutureVoidTask.add(CompletableFuture.runAsync(consumerToRunnable(consumer, t), executor));
        return this;
    }

    public FunctionAsyncTask<T, U, R> addTask(BiConsumer<? super T, ? super U> biConsumer, T t, U u) {
        completableFutureVoidTask.add(CompletableFuture.runAsync(consumerToRunnable(biConsumer, t, u), executor));
        return this;
    }

    public FunctionAsyncTask<T, U, R> addTask(Runnable runnable) {
        completableFutureVoidTask.add(CompletableFuture.runAsync(runnable, executor));
        return this;
    }


    /**
     * 执行任务
     *
     * @return 当使用有返回值的异步任务执行方法时返回执行结果
     */
    public List<R> exec() {
        List<R> result = new ArrayList<>();
        try {
            result = execTask();
        } catch (ExecutionException | InterruptedException e) {
            log.error("StreamTask_exec error", e);
        }
        return filterNonNull(result);
    }
}
