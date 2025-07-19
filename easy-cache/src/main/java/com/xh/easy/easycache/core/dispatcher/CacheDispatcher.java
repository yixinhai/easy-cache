package com.xh.easy.easycache.core.dispatcher;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xh.easy.easycache.base.ResultHandler;
import com.xh.easy.easycache.core.executor.executor.CacheExecutorWrapper;
import com.xh.easy.easycache.core.executor.executor.FaultTolerance;
import com.xh.easy.easycache.core.executor.executor.MultiLevelCacheExecutor;
import com.xh.easy.easycache.core.executor.handler.CacheBuilder;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import com.xh.easy.easycache.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.xh.easy.easycache.entity.constant.CacheConfigConstant.*;
import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;
import static com.xh.easy.easycache.entity.constant.LuaExecResult.*;

/**
 * 缓存调度器
 *
 * @author yixinhai
 */
@Slf4j
@Service
public class CacheDispatcher {

    /**
     * 查询缓存调度
     *
     * @param context 缓存上下文
     * @return 缓存内容
     */
    public Object doDispatch(QueryContext context) {
        try (ThreadLocalManager manager = new ThreadLocalManager()) {
            String key = context.getKey();

            // 获取缓存内容，redis缓存宕机处理
            CacheInfo info = manager.getCacheExecutor().getValue(context);
            log.info("{} act=CacheDispatcher_doDispatch msg=缓存结果：key={} info={}", LOG_STR, key, info);

            if (Objects.isNull(info)) {
                return ResultHandler.defaultResult(context);
            }

            // 记录开始处理结果时间
            manager.setStartTime(System.currentTimeMillis());

            // 处理结果
            return handleQueryResult(context, info, manager);
        }
    }

    /**
     * 处理查询结果
     *
     * @param context 缓存上下文
     * @param info 缓存信息
     * @param manager ThreadLocal管理器
     * @return 缓存内容
     */
    private Object handleQueryResult(QueryContext context, CacheInfo info, ThreadLocalManager manager) {
        // 更新缓存处理器
        manager.getCacheExecutor().setCacheExecutor(info.getCacheExecutor());

        // 处理查询结果
        String execResult = info.getExecResult();
        log.info("{} act=handleQueryResult msg=查询结果：key={} execResult={}", LOG_STR, context.getKey(), execResult);

        switch (execResult) {
            case SUCCESS -> {
                return manager.getCacheExecutor().hit(context, info);
            }
            case NEED_QUERY -> {
                return manager.getCacheExecutor().miss(context);
            }
            case SUCCESS_NEED_QUERY -> {
                return manager.getCacheExecutor().timeoutHit(context, info);
            }
            case NEED_WAIT -> {
                if (System.currentTimeMillis() - manager.getStartTime() > MAX_RETRY_TIME) {
                    log.warn("{} act=handleQueryResult msg=查询请求超过最大重试次数 key={}", LOG_STR, context.getKey());
                    return ResultHandler.defaultResult(context);
                }

                ThreadUtil.sleep(RETRY_TIME_INTERVAL);
                CacheInfo waitInfo = manager.getCacheExecutor().wait(context);
                return handleQueryResult(context, waitInfo, manager);
            }
            default -> {
                log.error("{} act=handleQueryResult msg=未知的查询结果 key={} execResult={}", LOG_STR, context.getKey(),
                    execResult);
                return ResultHandler.defaultResult(context);
            }
        }
    }

    /**
     * 更新缓存调度
     *
     * @param context 缓存上下文
     * @return 目标方法返回值
     */
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
}

/**
 * ThreadLocal管理器
 *
 * @author yixinhai
 */
@Slf4j
class ThreadLocalManager implements AutoCloseable {
    private final ThreadLocal<CacheExecutorWrapper<MultiLevelCacheExecutor>> cacheExecutor;
    private final ThreadLocal<Long> startTime;

    public ThreadLocalManager() {
        this.cacheExecutor = new TransmittableThreadLocal<>() {
            @Override
            protected FaultTolerance<MultiLevelCacheExecutor> initialValue() {
                return new FaultTolerance<>(CacheBuilder.getHandler());
            }
        };
        this.startTime = new TransmittableThreadLocal<>();
    }

    public CacheExecutorWrapper<MultiLevelCacheExecutor> getCacheExecutor() {
        return cacheExecutor.get();
    }

    public void setStartTime(Long time) {
        startTime.set(time);
    }

    public Long getStartTime() {
        return startTime.get();
    }

    @Override
    public void close() {
        try {
            cacheExecutor.remove();
            startTime.remove();
        } catch (Exception e) {
            log.warn("{} act=ThreadLocalManager_close msg=清理ThreadLocal资源失败", LOG_STR, e);
        }
    }
}
