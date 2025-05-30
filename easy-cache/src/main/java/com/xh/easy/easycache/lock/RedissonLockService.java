package com.xh.easy.easycache.lock;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 分布式锁
 *
 * @author yixinhai
 */
@Component
public class RedissonLockService {


    /**
     * 根据分布式锁的名称执行指定的方法
     * 若未获取到锁，当前方法默认在5秒内每100ms尝试获取锁
     *
     * @param lockName 分布式锁名称
     * @param supplier 要执行的方法
     * @param <R>      返回值类型
     * @return R 若获取锁成功，则返回执行方法返回值，否则返回null
     */
    public <R> R executeTryLock(String lockName, Supplier<R> supplier) {
        return supplier.get();
    }

    /**
     * 根据分布式锁的名称执行指定的方法
     * 若未获取到锁，当前方法默认在5秒内每100ms尝试获取锁
     *
     * @param lockName    分布式锁名称
     * @param runnable    要执行的方法
     */
    public void executeTryLock(String lockName, Runnable runnable) {
        return;
    }
}
