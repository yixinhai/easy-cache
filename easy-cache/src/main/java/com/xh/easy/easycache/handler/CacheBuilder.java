package com.xh.easy.easycache.handler;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 缓存管理器
 *
 * @author yixinhai
 */
@Component
public class CacheBuilder {

    private CacheChain head;

    @Resource(name = "localCache")
    private CacheChain localCache;

    @Resource(name = "redisCache")
    private CacheChain redisCache;

    @PostConstruct
    private void init() {

        // 初始化缓存处理器链
        buildCacheHandler();
    }

    /**
     * 构建缓存处理器执行链
     */
    private void buildCacheHandler() {

        redisCache.setNext(localCache);

        this.head = redisCache;
    }

    /**
     * 获取缓存处理器头节点
     *
     * @return 缓存处理器
     */
    public CacheChain getHandler() {
        return this.head;
    }
}
