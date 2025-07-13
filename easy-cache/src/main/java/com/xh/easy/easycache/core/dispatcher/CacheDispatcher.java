package com.xh.easy.easycache.core.dispatcher;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xh.easy.easycache.base.ResultHandler;
import com.xh.easy.easycache.core.executor.*;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 缓存调度器
 *
 * @author yixinhai
 */
@Slf4j
@Service
public class CacheDispatcher {

    private static final ThreadLocal<CacheExecutorWrapper<MultiLevelCacheExecutor>> cacheExecutor =
        new TransmittableThreadLocal<>() {
            @Override
            protected FaultTolerance<MultiLevelCacheExecutor> initialValue() {
                return new FaultTolerance<>(CacheBuilder.getHandler());
            }
        };


    /**
     * 查询缓存调度
     *
     * @param context 缓存上下文
     * @return 缓存内容
     */
    public Object doDispatch(QueryContext context) {

        String key = context.getKey();

        // 获取缓存内容，redis缓存宕机处理
        CacheInfo info = cacheExecutor.get().getValue(context);
        log.info("{} act=CacheDispatcher_doDispatch msg=缓存结果：key={} info={}", LOG_STR, key, info);

        if (Objects.isNull(info)) {
            return ResultHandler.defaultResult(context);
        }

        // 处理结果
        return handleQueryResult(context, info);
    }

    /**
     * 处理查询结果
     *
     * @param context 缓存上下文
     * @param info 缓存信息
     * @return 缓存内容
     */
    private Object handleQueryResult(QueryContext context, CacheInfo info) {
        // 更新缓存处理器
        cacheExecutor.get().setCacheExecutor(info.getCacheExecutor());

        // 处理查询结果
        return StringUtils.hasText(info.getValue()) ? cacheExecutor.get().hit(context, info)
            : cacheExecutor.get().miss(context);
    }

    /**
     * 更新缓存调度
     *
     * @param context 缓存上下文
     * @return 目标方法返回值
     */
    public Object doDispatch(UpdateContext context) {

        String key = context.getKey();

        // TODO 记录本地消息表，启动服务时进行流量回放

        try {
            return context.proceed();
        } catch (TargetMethodExecFailedException e) {
            log.error("act=CacheDispatcher_doDispatch msg=目标方法执行异常 key={}", key, e);
        } finally {
            // 上报缓存更新
            cacheExecutor.get().lockCacheInfo(context);
        }

        return ResultHandler.defaultResult(context);
    }
}
