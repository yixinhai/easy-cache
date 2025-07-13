package com.xh.easy.easycache.core.executor;

import com.xh.easy.easycache.entity.context.CacheContext;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.entity.model.CacheInfo;

/**
 * 更新缓存
 *
 * @author yixinhai
 */
public interface CacheWriter {

    /**
     * 命中缓存
     *
     * @param context
     *     缓存执行上下文
     * @param info
     *     缓存信息
     * @return 缓存数据
     */
    Object hit(QueryContext context, CacheInfo info);

    /**
     * 未命中缓存
     *
     * @param context
     *     缓存执行上下文
     * @return 缓存数据
     */
    Object miss(QueryContext context);

    /**
     * 失效缓存
     *
     * @param context
     *     缓存执行上下文
     * @return 是否失效成功
     */
    boolean invalid(CacheContext context);

    /**
     * 缓存锁定处理
     *
     * @param context 缓存上下文
     */
    void lockCacheInfo(UpdateContext context);
}
