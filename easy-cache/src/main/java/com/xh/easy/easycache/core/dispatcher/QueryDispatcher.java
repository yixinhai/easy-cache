package com.xh.easy.easycache.core.dispatcher;

import com.xh.easy.easycache.entity.context.QueryContext;

/**
 * 查询缓存调度器
 *
 * @author yixinhai
 */
public interface QueryDispatcher {

    /**
     * 查询缓存调度
     *
     * @param context 查询缓存上下文
     * @return 缓存查询结果
     */
    Object doDispatch(QueryContext context);
}
