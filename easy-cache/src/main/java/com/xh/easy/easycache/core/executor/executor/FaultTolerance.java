package com.xh.easy.easycache.core.executor.executor;

import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.entity.model.CacheResult;
import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import lombok.extern.slf4j.Slf4j;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 缓存容错处理
 *
 * @author yixinhai
 */
@Slf4j
public class FaultTolerance<T extends MultiLevelCacheExecutor> extends CacheExecutorWrapper<T> {

    public FaultTolerance(T t) {
        this.cacheExecutor = t;
    }

    @Override
    public void setCacheExecutor(T t) {
        this.cacheExecutor = t;
    }

    @Override
    public CacheInfo getValue(QueryContext context) {
        String key = context.getKey();
        if (key == null || key.isEmpty()) {
            log.error("{} act=getValue msg=缓存key参数不合法", LOG_STR);
            return null;
        }

        // 获取目标方法返回值
        CacheInfo info = cacheExecutor.loadValue(context);

        // 缓存穿透处理
        if (this.cachePenetration(context, info)) {
            log.info("{} act=getValue msg=命中缓存穿透策略，返回null，key={}", LOG_STR, key);
            return null;
        }

        return info;
    }

    @Override
    public CacheInfo wait(QueryContext context) {
        return cacheExecutor.wait(context);
    }

    @Override
    public Object hit(QueryContext context, CacheInfo info) {

        // 若缓存内容为防缓存穿透默认值，返回null
        if (info.isDefaultNullValue()) {
            log.info("{} act=getValue msg=防止缓存穿透，返回null，key={}", LOG_STR, context.getKey());
            return null;
        }

        return cacheExecutor.hit(context, info);
    }

    @Override
    public Object timeoutHit(QueryContext context, CacheInfo info) {
        return cacheExecutor.timeoutHit(context, info);
    }

    @Override
    public Object miss(QueryContext context) {

        Object o = null;
        try {
            o = context.proceed();
        } catch (Exception e) {
            log.warn("{} act=miss msg=目标方法执行异常 key={}", LOG_STR, context.getKey(), e);
            throw new TargetMethodExecFailedException("目标方法执行异常", e);
        }

        if (o == null && context.cachePenetration()) {
            o = CacheResult.NULL;
        }

        return cacheExecutor.setValue(context, o);
    }

    @Override
    public void lockCacheInfo(UpdateContext context) {
        cacheExecutor.lockCacheInfo(context);
    }

    @Override
    public boolean ping(String clusterId) {
        return cacheExecutor.ping(clusterId);
    }

    /**
     * 是否命中穿透处理
     *
     * @param context 缓存上下文
     * @param info 缓存信息
     * @return 是否开启并命中缓存穿透
     */
    private boolean cachePenetration(QueryContext context, CacheInfo info) {
        return context.cachePenetration() && info.isDefaultNullValue();
    }
}
