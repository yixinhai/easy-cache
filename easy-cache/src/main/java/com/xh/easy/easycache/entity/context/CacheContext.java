package com.xh.easy.easycache.entity.context;

import com.xh.easy.easycache.exception.TargetMethodExecFailedException;

/**
 * 缓存上下文
 *
 * @author yixinhai
 */
public interface CacheContext {

    /**
     * 获取缓存key
     *
     * @return 缓存key
     */
    String getKey();

    /**
     * 获取缓存集群id
     *
     * @return 缓存集群id
     */
    String getClusterId();

    /**
     * 执行目标方法
     *
     * @return 执行目标方法返回值
     * @throws TargetMethodExecFailedException 目标方法执行异常
     */
    Object proceed() throws TargetMethodExecFailedException;
}
