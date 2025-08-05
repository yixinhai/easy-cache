package com.xh.easy.easycache.core.dispatcher;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xh.easy.easycache.core.executor.executor.*;
import com.xh.easy.easycache.core.executor.handler.CacheBuilder;
import com.xh.easy.easycache.core.result.ResultHandler;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 缓存调度器
 *
 * @author yixinhai
 */
@Slf4j
@Service
public class CacheDispatcher implements QueryDispatcher, UpdateDispatcher {

    @Autowired
    private ResultHandler resultHandler;

    @Override
    public Object doDispatch(QueryContext context) {
        try (ThreadLocalManager manager = new ThreadLocalManager()) {
            String key = context.getKey();

            // 获取缓存内容，redis缓存宕机处理
            CacheInfo info = manager.getCacheExecutor().getValue(context);
            log.info("{} act=CacheDispatcher_doDispatch msg=缓存结果：key={} info={}", LOG_STR, key, info);

            if (Objects.isNull(info)) {
                return resultHandler.defaultResult(context);
            }

            // 更新缓存处理器
            changeCacheExecutor(info, manager);

            // 处理结果
            return resultHandler.handleResult(context, info, manager.getCacheExecutor(), System.currentTimeMillis());
        }
    }

    @Override
    public Object doDispatch(UpdateContext context) {
        try (ThreadLocalManager manager = new ThreadLocalManager()) {
            String key = context.getKey();

            // TODO 记录本地消息表，启动服务时进行流量回放

            try {
                return context.proceed();
            } catch (Throwable e) {
                log.warn("act=CacheDispatcher_doDispatch msg=目标方法执行异常 key={}", key, e);
                throw new TargetMethodExecFailedException("msg=目标方法执行异常", e);
            } finally {
                // 上报缓存更新
                manager.getCacheExecutor().lockCacheInfo(context);
            }
        }
    }

    /**
     * 更新缓存处理器
     *
     * @param info 缓存信息
     * @param manager ThreadLocal管理器
     */
    private void changeCacheExecutor(CacheInfo info, ThreadLocalManager manager) {
        manager.getCacheExecutor().changeCacheExecutor(info.getCacheExecutor());
    }
}

/**
 * ThreadLocal管理器
 *
 * @author yixinhai
 */
@Slf4j
class ThreadLocalManager implements AutoCloseable {
    private final ThreadLocal<CacheExecutorWrapper<MultiLevelCacheExecutor>> cacheExecutor;

    public ThreadLocalManager() {
        this.cacheExecutor = new TransmittableThreadLocal<>() {
            @Override
            protected FaultTolerance<MultiLevelCacheExecutor> initialValue() {
                return new FaultTolerance<>(CacheBuilder.build().getHandler());
            }
        };
    }

    public CacheExecutorWrapper<MultiLevelCacheExecutor> getCacheExecutor() {
        return cacheExecutor.get();
    }

    @Override
    public void close() {
        try {
            cacheExecutor.remove();
        } catch (Exception e) {
            log.warn("{} act=ThreadLocalManager_close msg=清理ThreadLocal资源失败", LOG_STR, e);
        }
    }
}
