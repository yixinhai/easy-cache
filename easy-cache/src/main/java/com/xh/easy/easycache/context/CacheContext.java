package com.xh.easy.easycache.context;

import com.xh.easy.easycache.exception.TargetMethodExecFailedException;

/**
 * 缓存上下文
 *
 * @author yixinhai
 */
public interface CacheContext {

    String getKey();

    String getClusterId();

    Object proceed() throws TargetMethodExecFailedException;
}
