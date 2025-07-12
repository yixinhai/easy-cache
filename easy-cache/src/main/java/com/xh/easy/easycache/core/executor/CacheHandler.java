package com.xh.easy.easycache.core.executor;

import com.xh.easy.easycache.entity.context.CacheContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;

/**
 * 缓存处理器
 *
 * @author yixinhai
 */
public abstract class CacheHandler {

    /**
     * 同步set缓存
     *
     * @param context 缓存上下文
     * @param o 目标value
     * @return 目标value
     */
    protected abstract Object setValue(QueryContext context, Object o);

    /**
     * ping
     */
    protected abstract boolean ping(String clusterId);

    /**
     * 失效缓存
     *
     * @param context 缓存上下文
     * @return 是否失效成功
     */
    protected abstract boolean invalid(CacheContext context);

    /**
     * 获取缓存数据
     *
     * @param context
     *     缓存执行上下文
     * @return 缓存数据
     */
    protected abstract CacheInfo loadValue(QueryContext context);
}
