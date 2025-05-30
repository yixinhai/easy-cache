package com.xh.easy.easycache.async;

import com.xh.easy.easycache.base.LambdaConverter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public abstract class AsyncTask<T, U, R> extends LambdaConverter {

    /**
     * 无返回值任务类型
     */
    protected static final Integer TASK_VOID = 0;

    /**
     * 指定返回值类型任务类型
     */
    protected static final Integer TASK_R = 1;

    /**
     * 任务类型
     */
    protected Integer taskType;

    /**
     * 指定返回值类型任务列表
     */
    protected final List<CompletableFuture<R>> completableFutureTask = new ArrayList<>();

    /**
     * 无返回值类型任务列表
     */
    protected final List<CompletableFuture<Void>> completableFutureVoidTask = new ArrayList<>();

    protected Executor executor = ForkJoinPool.commonPool();


    /**
     * 执行任务
     * @return List<R> 执行结果
     */
    protected List<R> execTask() throws ExecutionException, InterruptedException {
        List<R> result = new ArrayList<>();
        if (Objects.equals(taskType, TASK_VOID)) {
            completableFutureVoidTask.forEach(CompletableFuture::join);
        } else if (Objects.equals(taskType, TASK_R)) {
            result = completableFutureTask.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }
        return result;
    }

    protected <L> List<L> filterNonNull(List<L> source) {
        return Optional.ofNullable(source).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
