package com.xh.easy.easycache.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 分布式锁
 *
 * @author yixinhai
 */
@Slf4j
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
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("{} act=executeTryLock msg=执行方法异常", LOG_STR, e);
            return null;
        }
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
