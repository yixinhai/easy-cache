package com.xh.easy.easycache.core.executor.executor;

import com.xh.easy.easycache.core.executor.handler.CacheBuilder;
import com.xh.easy.easycache.core.monitor.healthy.event.UpdateFailed;
import com.xh.easy.easycache.core.monitor.healthy.event.UpdateSuccess;
import com.xh.easy.easycache.entity.context.CacheContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import com.xh.easy.easycache.utils.async.FunctionAsyncTask;
import lombok.extern.slf4j.Slf4j;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 多级缓存执行器
 *
 * @author yixinhai
 */
@Slf4j
public abstract class MultiLevelCacheExecutor implements CacheExecutor {

    @Override
    public Object hit(QueryContext context, CacheInfo info) {
        return info.getValue(context.getResultType());
    }

    @Override
    public Object timeoutHit(QueryContext context, CacheInfo info) {
        FunctionAsyncTask.getRunAsyncInstance()
            .addTask(() -> setValue(context, info));

        return hit(context, info);
    }

    @Override
    public Object miss(QueryContext context) {
        // 未命中缓存，执行目标方法并记录缓存
        try {
            return setValue(context, context.proceed());
        } catch (Throwable e) {
            log.warn("{} act=miss msg=目标方法执行异常 key={}", LOG_STR, context.getKey());
            throw new TargetMethodExecFailedException("目标方法执行异常", e);
        }
    }

    @Override
    public CacheInfo wait(QueryContext context) {
        return loadValue(context);
    }

    @Override
    public void lockCacheInfo(UpdateContext context) {
        CacheBuilder.build().getAllHandler().forEach(handler -> {
            try {
                if (!invalid(context)) {
                    updateFailed(context, handler);
                }

                updateSuccess(context, handler);
            } catch (Exception e) {
                updateFailed(context, handler);
            }
        });
    }

    /**
     * 失效缓存失败
     *
     * @param context
     *     缓存执行上下文
     * @param handler
     *     失效缓存执行器
     */
    private void updateFailed(UpdateContext context, CacheExecutor handler) {
        log.warn("{} act=lockCacheInfo msg=失效缓存失败，当前key降级 clusterId={} key={} handler={}",
            LOG_STR, context.getClusterId(), context.getKey(), handler.getClass().getName());

        new UpdateFailed(context.getKey(), handler).accept();
    }

    /**
     * 失效缓存成功
     *
     * @param context
     *     缓存执行上下文
     * @param handler
     *     失效缓存执行器
     */
    private void updateSuccess(UpdateContext context, CacheExecutor handler) {
        log.warn("{} act=lockCacheInfo msg=失效缓存成功，当前key升级 clusterId={} key={} handler={}",
            LOG_STR, context.getClusterId(), context.getKey(), handler.getClass().getName());

        new UpdateSuccess(context.getKey(), handler).accept();
    }

    /**
     * 设置缓存
     *
     * @param context 缓存上下文
     * @param o 目标方法返回值
     * @return 目标方法返回值
     */
    public abstract Object setValue(QueryContext context, Object o);

    /**
     * 获取缓存内容
     *
     * @param context 缓存上下文
     * @return 缓存信息
     */
    public abstract CacheInfo loadValue(QueryContext context);

    /**
     * 失效缓存
     *
     * @param context
     *     缓存执行上下文
     * @return 是否失效成功
     */
    protected abstract boolean invalid(CacheContext context);
}
