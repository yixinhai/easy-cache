package com.xh.easy.easycache.core.executor;

import java.util.List;

/**
 * 缓存管理器
 *
 * @author yixinhai
 */
public class CacheBuilder {

    private static CacheChain head;

    private static final CacheChain localCache = LocalCache.getInstance();

    private static final CacheChain redisCache = RedisCache.getInstance();

    private static final List<CacheChain> cacheHandlers = List.of(localCache, redisCache);

    static {

        // 初始化缓存处理器链
        buildCacheHandler();
    }

    /**
     * 构建缓存处理器执行链
     */
    private static void buildCacheHandler() {

        redisCache.setNext(localCache);

        head = redisCache;
    }

    /**
     * 获取缓存处理器头节点
     *
     * @return 缓存处理器
     */
    public static CacheChain getHandler() {
        return head;
    }

    public static List<CacheChain> getAllHandler() {
        return cacheHandlers;
    }
}
