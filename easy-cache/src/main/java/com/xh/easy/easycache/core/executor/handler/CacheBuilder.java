package com.xh.easy.easycache.core.executor.handler;

import java.util.Arrays;
import java.util.List;

/**
 * 缓存管理器
 *
 * @author yixinhai
 */
public class CacheBuilder {

    private static final CacheBuilder INSTANCE = new CacheBuilder();

    private CacheChain head;

    private final CacheChain localCache = LocalCache.getInstance();

    private final CacheChain redisCache = RedisCache.getInstance();

    private final List<CacheChain> cacheHandlers = Arrays.asList(redisCache, localCache);

    private CacheBuilder() {
        // 初始化缓存处理器链
        buildCacheHandler();
    }

    public static CacheBuilder build() {
        return INSTANCE;
    }

    /**
     * 构建缓存处理器执行链
     */
    private void buildCacheHandler() {

        redisCache.setNext(localCache);

        head = redisCache;
    }

    /**
     * 获取缓存处理器头节点
     *
     * @return 缓存处理器
     */
    public CacheChain getHandler() {
        return head;
    }

    public List<CacheChain> getAllHandler() {
        return cacheHandlers;
    }
}
