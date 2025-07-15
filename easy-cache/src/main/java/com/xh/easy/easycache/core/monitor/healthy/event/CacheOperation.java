package com.xh.easy.easycache.core.monitor.healthy.event;

import com.xh.easy.easycache.core.executor.executor.CacheExecutor;
import com.xh.easy.easycache.core.executor.handler.RedisCache;

/**
 * 缓存事件
 *
 * @author yixinhai
 */
public abstract class CacheOperation implements Element {

    protected final CacheExecutor executor;

    public CacheOperation(CacheExecutor executor) {
        this.executor = executor;
    }

    /**
     * 是否集群触发事件
     * @return true: 集群触发，false: 本地缓存触发
     */
    public boolean sendByCluster() {
        return executor instanceof RedisCache;
    }
}
