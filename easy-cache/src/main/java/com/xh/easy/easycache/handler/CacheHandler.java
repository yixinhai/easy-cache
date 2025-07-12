package com.xh.easy.easycache.handler;

import com.xh.easy.easycache.context.CacheContext;
import com.xh.easy.easycache.context.CacheInfo;
import com.xh.easy.easycache.context.QueryContext;

/**
 * 缓存处理器
 *
 * @author yixinhai
 */
public abstract class CacheHandler {

    /**
     * 获取缓存信息
     *
     * @param context 缓存上下文
     */
    abstract CacheInfo getValue(QueryContext context);

    /**
     * 同步set缓存
     *
     * @param context 缓存上下文
     * @param o 目标value
     * @return 目标value
     */
    abstract Object setValue(QueryContext context, Object o);

    /**
     * ping
     */
    abstract boolean ping(String clusterId);

    /**
     * 失效缓存
     *
     * @param context 缓存上下文
     * @return 是否失效成功
     */
    abstract boolean invalid(CacheContext context);
}
