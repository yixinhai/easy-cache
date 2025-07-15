package com.xh.easy.easycache.core.executor.executor;

import com.alibaba.fastjson.JSON;
import com.xh.easy.easycache.utils.async.FunctionAsyncTask;
import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.utils.RedissonLockService;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 缓存容错处理
 *
 * @author yixinhai
 */
@Slf4j
public class FaultTolerance<T extends MultiLevelCacheExecutor> extends CacheExecutorWrapper<T> {

    private final RedissonLockService lock;

    public FaultTolerance(T t) {
        this.cacheExecutor = t;
        this.lock = ClassHandler.getBeanByType(RedissonLockService.class);
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
    public Object hit(QueryContext context, CacheInfo info) {

        // 若缓存内容为防缓存穿透默认值，返回null
        if (info.isDefaultNullValue()) {
            log.info("{} act=getValue msg=防止缓存穿透，返回null，key={}", LOG_STR, context.getKey());
            return null;
        }

        Object o = cacheExecutor.hit(context, info);

        // 若是l2缓存获取的返回值，直接返回
        if (info.isL2Cache()) {
            log.info("{} act=hit msg=请求命中l2缓存 result={}", LOG_STR, JSON.toJSONString(o));
            return o;
        }

        // 缓存信息和数据库一致，直接返回
        if (info.coherent()) {
            log.info("{} act=hit msg=请求命中缓存 result={}", LOG_STR, JSON.toJSONString(o));
            return o;
        }

        // 在延迟删除时效内，尝试更新缓存
        if (info.lockTimeout()) {
            setValueAsync(context);
            log.info("{} act=hit msg=请求命中缓存 result={}", LOG_STR, JSON.toJSONString(o));
            return o;
        }

        // 在延迟删除实效外，同步更新缓存，重新执行目标方法并返回
        return setValue(context);
    }

    @Override
    public Object miss(QueryContext context) {
        return lock.executeTryLock(context.getKey(), () -> {

            // 再次检查缓存，防止其他线程已经更新了缓存
            CacheInfo info = getValue(context);
            if (Objects.nonNull(info) && info.coherent()) {
                log.info("{} act=miss msg=请求命中缓存 cacheInfo={}", LOG_STR, info);
                return info.getValue(context.getResultType());
            }

            // 未命中缓存，执行目标方法并记录缓存
            return cacheExecutor.miss(context);
        });
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

    /**
     * 异步写入缓存
     *
     * @param context 缓存上下文
     */
    private void setValueAsync(QueryContext context) {
        FunctionAsyncTask.getRunAsyncInstance()
            .addTask(() -> {
                cacheExecutor.setValue(context, context.proceed());
            }).exec();
    }

    /**
     * 同步写入缓存
     *
     * @param context 缓存上下文
     */
    private Object setValue(QueryContext context) {
        return cacheExecutor.setValue(context, context.proceed());
    }
}
