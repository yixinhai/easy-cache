package com.xh.easy.easycache.core.result;

import com.xh.easy.easycache.base.JoinPointContext;
import com.xh.easy.easycache.core.executor.executor.CacheExecutor;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.model.CacheInfo;

import java.util.Objects;

/**
 * 缓存结果处理器
 *
 * @author yixinhai
 */
public abstract class ResultHandler {

    /**
     * 默认结果
     *
     * @param context aop上下文
     * @return 默认值
     */
    public <T extends JoinPointContext> Object defaultResult(T context) {
        Class<?> resultType = context.getResultClass();
        if (Objects.equals(resultType, Boolean.TYPE)) {
            return false;
        }
        return resultType.isPrimitive() ? 0 : null;
    }

    /**
     * 处理查询结果
     *
     * @param context 缓存上下文
     * @param info 缓存信息
     * @param cacheExecutor 缓存执行器
     * @param startTime 处理开始时间
     * @return 缓存内容
     */
    public abstract Object handleResult(QueryContext context, CacheInfo info, CacheExecutor cacheExecutor,
        long startTime);
}
