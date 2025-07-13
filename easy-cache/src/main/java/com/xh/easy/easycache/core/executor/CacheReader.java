package com.xh.easy.easycache.core.executor;

import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.model.CacheInfo;

/**
 * 读取缓存
 *
 * @author yixinhai
 */
public interface CacheReader {

    /**
     * 获取缓存数据
     *
     * @param context
     *     缓存执行上下文
     * @return 缓存数据
     */
    CacheInfo getValue(QueryContext context);
}
