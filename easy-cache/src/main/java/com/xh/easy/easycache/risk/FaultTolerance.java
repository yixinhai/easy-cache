package com.xh.easy.easycache.risk;

import com.alibaba.fastjson.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.xh.easy.easycache.context.CacheInfo;
import com.xh.easy.easycache.context.QueryContext;
import com.xh.easy.easycache.context.UpdateContext;
import com.xh.easy.easycache.handler.CacheExecutor;
import com.xh.easy.easycache.handler.CacheHandler;
import com.xh.easy.easycache.risk.event.ClusterFault;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 缓存容错处理
 *
 * @author yixinhai
 */
@Slf4j
@Component
public class FaultTolerance {

    private static final ThreadLocal<CacheHandler> cacheHandler = new TransmittableThreadLocal<>();

    @Autowired
    private List<CacheHandler> cacheHandlers;

    @Autowired
    private CacheExecutor cacheExecutor;

    @Autowired
    private CacheHandler redisCache;

    @Autowired
    private CacheHandler localCache;

    @Autowired
    private FaultManager faultManager;


    /**
     * 缓存安全获取
     *
     * @param context 缓存上下文
     * @return 缓存信息
     */
    public CacheInfo safelyGetValue(QueryContext context) {

        String key = context.getKey();
        if (key == null || key.isEmpty()) {
            log.error("act=safelyGetValue msg=缓存key参数不合法");
            return null;
        }

        // 获取目标方法返回值
        CacheInfo info = faultManager.isClusterAvailable(key, context.getClusterId()) ? handleClusterAvailable(context)
                : handleClusterNotAvailable(context);

        // 缓存穿透处理
        if (this.cachePenetration(context, info)) {
            log.info("act=CacheDispatcher_doDispatch msg=命中缓存穿透策略，返回null，key={}", key);
            return null;
        }

        return info;
    }

    /**
     * 集群不可用情况缓存安全获取
     *
     * @param context 缓存上下文
     * @return 缓存信息
     */
    private CacheInfo handleClusterNotAvailable(QueryContext context) {

        String key = context.getKey();
        CacheInfo info = null;

        // 开启l2缓存，尝试从l2缓存获取
        if (context.enableL2Cache()) {
            log.info("act=handleClusterNotAvailable msg=分布式缓存降级，尝试从l2缓存获取 key={}", key);
            cacheHandler.set(localCache);
            info = cacheExecutor.getValue(context, localCache);
        }

        return info;
    }

    /**
     * 集群可用情况缓存安全获取
     *
     * @param context 缓存上下文
     * @return 缓存信息
     */
    private CacheInfo handleClusterAvailable(QueryContext context) {

        String clusterId = context.getClusterId();
        String key = context.getKey();
        CacheInfo info = null;

        // 尝试从redis获取缓存数据
        try {
            cacheHandler.set(redisCache);
            info = cacheExecutor.getValue(context, redisCache);
        } catch (Exception e) {
            log.error("act=handleClusterAvailable msg=从redis获取缓存数据异常 key={}", key, e);

            // 更新故障信息
            new ClusterFault(clusterId).accept();

            // redis宕机处理，l2缓存兜底
            if (context.enableL2Cache()) {
                log.info("act=handleClusterAvailable msg=从redis获取缓存数据异常，尝试从l2缓存获取 key={}", key, e);
                cacheHandler.set(localCache);
                info = cacheExecutor.getValue(context, localCache);
            }
        }

        return info;
    }


    /**
     * 缓存穿透处理
     *
     * @param context 缓存上下文
     * @param info 缓存信息
     * @return 是否开启并命中缓存穿透
     */
    public boolean cachePenetration(QueryContext context, CacheInfo info) {
        return context.cachePenetration() && info.isDefaultNullValue();
    }

    /**
     * 缓存命中处理
     *
     * @param context 缓存上下文
     * @param info 缓存信息
     * @return 缓存命中结果
     */
    public Object hit(QueryContext context, CacheInfo info) {
        Object o = info.getValue(context.getResultType());

        // 若是l2缓存获取的返回值，直接返回
        if (info.isL2Cache()) {
            log.info("act=hit msg=请求命中l2缓存 result={}", JSON.toJSONString(o));
            return o;
        }

        // 缓存信息和数据库一致，直接返回
        if (info.coherent()) {
            log.info("act=hit msg=请求命中缓存 result={}", JSON.toJSONString(o));
            return o;
        }

        // 在延迟删除时效内，尝试更新缓存
        if (info.lockTimeout()) {
            cacheExecutor.setValueAsync(context, cacheHandler.get());
            log.info("act=hit msg=请求命中缓存 result={}", JSON.toJSONString(o));
            return o;
        }

        // 在延迟删除实效外，同步更新缓存
        return cacheExecutor.setValue(context, cacheHandler.get());
    }

    /**
     * 缓存未命中处理
     *
     * @param context 缓存上下文
     * @return 目标方法返回结果
     */
    public Object miss(QueryContext context) {
        return cacheExecutor.setValue(context, cacheHandler.get());
    }

    /**
     * 缓存锁定处理
     *
     * @param context 缓存上下文
     */
    public void lockCacheInfo(UpdateContext context) {
        cacheHandlers.forEach(handler -> cacheExecutor.invalid(context, handler));
    }
}
