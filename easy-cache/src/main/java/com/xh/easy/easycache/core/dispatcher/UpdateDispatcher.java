package com.xh.easy.easycache.core.dispatcher;

import com.xh.easy.easycache.entity.context.UpdateContext;

/**
 * 更新源数据调度器
 *
 * @author yixinhai
 */
public interface UpdateDispatcher {

    /**
     * 更新源数据调度
     *
     * @param context 更新数据上下文
     * @return 目标方法执行结果
     */
    Object doDispatch(UpdateContext context);
}
