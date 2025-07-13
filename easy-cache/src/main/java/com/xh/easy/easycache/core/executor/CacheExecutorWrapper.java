package com.xh.easy.easycache.core.executor;

/**
 * 缓存执行装饰器
 *
 * @author yixinhai
 */
public abstract class CacheExecutorWrapper<T extends CacheExecutor> implements CacheExecutor {

    /**
     * 缓存执行器
     */
    protected T cacheExecutor;

    /**
     * 设置被装饰的缓存执行器
     *
     * @param t 缓存执行器
     */
    public abstract void setCacheExecutor(T t);
}
