package com.xh.easy.easycache.core.executor.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xh.easy.easycache.entity.context.CacheContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.utils.RandomUtils;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存处理器
 *
 * @author yixinhai
 */
public class LocalCache extends CacheChain {

    private static final LocalCache INSTANCE = new LocalCache();

    private static final Cache<String, String> l2LocalCache = CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private LocalCache() {
    }

    public static LocalCache getInstance() {
        return INSTANCE;
    }

    @Override
    public CacheInfo getValue(QueryContext context) {
        return new CacheInfo(l2LocalCache.getIfPresent(context.getKey()), true, this);
    }

    @Override
    public Object setValue(QueryContext context, Object o) {

        // 指定比例请求缓存至二级缓存
        int nextInt = 10;
        if (RandomUtils.generateRandom(System.currentTimeMillis(), nextInt) >= context.getL2CacheProportion()) {
            return o;
        }

        // 本地缓存
        l2LocalCache.put(context.getKey(), JSON.toJSONString(o));

        return o;
    }

    @Override
    public boolean ping(String clusterId) {
        return true;
    }

    @Override
    public boolean invalid(CacheContext context) {
        l2LocalCache.invalidate(context.getKey());
        return true;
    }

    @Override
    public boolean support(QueryContext context) {
        return context.enableL2Cache();
    }
}
