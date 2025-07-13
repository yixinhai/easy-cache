package com.xh.easy.easycache.core.executor.handler;

import com.xh.easy.easycache.config.ClusterConfiguration;
import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.entity.context.CacheContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.model.TimeInfo;
import com.xh.easy.easycache.core.monitor.healthy.ClusterHealthInfo;
import com.xh.easy.easycache.utils.serialze.SerializerManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.xh.easy.easycache.entity.model.CacheInfo.*;

/**
 * 集群缓存处理器
 *
 * @author yixinhai
 */
@Slf4j
public class RedisCache extends CacheChain {

    private static final RedisCache INSTANCE = new RedisCache();

    private final ClusterConfiguration clusterConfiguration;

    private RedisCache() {
        this.clusterConfiguration = ClassHandler.getBeanByType(ClusterConfiguration.class);
    }

    public static RedisCache getInstance() {
        return INSTANCE;
    }

    @Override
    public CacheInfo getValue(QueryContext context) {
        return CacheInfo.initByCacheMap(getService(context).hgetall(context.getKey()), this);
    }

    @Override
    public Object setValue(QueryContext context, Object o) {

        String key = context.getKey();

        // 若执行目标方法返回结果为null且未开启防缓存穿透，则只更新缓存的lockInfo
        if (o == null && !context.cachePenetration()) {
            log.info("act=setValue msg=未开启防缓存穿透且目标方法返回值为空，只更新lockInfo key={}", key);
            getService(context).hset(context.getKey(), HASH_FIELD_LOCK_INFO, UN_LOCK);
            return null;
        }

        // 缓存信息
        TimeInfo timeInfo = context.getTimeInfo();
        Map<String, String> value = this.convertValue(context, o);

        if (timeInfo.getExpireTime() > 0) {
            getService(context).hsetallnx(key, value, timeInfo.toSeconds());
        } else {
            getService(context).hsetall(key, value);
        }

        return o;
    }

    @Override
    public boolean ping(String clusterId) {
        return getService(clusterId).ping();
    }

    @Override
    public boolean invalid(CacheContext context) {
        CacheInfo info = new CacheInfo();
        info.setLockInfo(LOCKED);
        info.setUnlockTime(String.valueOf(System.currentTimeMillis() + getUnlockTime()));
        return "OK".equals(getService(context).hsetall(context.getKey(), info.parseCacheMap()));
    }

    @Override
    public boolean support(QueryContext context) {
        return ClusterHealthInfo.isClusterAvailable(context.getKey(), context.getClusterId());
    }

    /**
     * 将目标方法返回结果转换成缓存信息
     *
     * @param context 缓存上下文
     * @param o 目标方法返回结果
     * @return map格式缓存信息
     */
    private Map<String, String> convertValue(QueryContext context, Object o) {
        // 若开启缓存穿透且目标方法直接结果为null，缓存写入默认值
        String value = o == null ? NULL : SerializerManager.jsonSerializer().serialize2String(o);

        return new CacheInfo(value, UN_LOCK, this).parseCacheMap();
    }

    /**
     * 获取缓存服务类
     *
     * @param context 缓存上下文
     */
    private BaseRedisService getService(CacheContext context) {
    	return getService(context.getClusterId());
    }

    /**
     * 获取缓存服务类
     *
     * @param clusterId 集群ID
     */
    private BaseRedisService getService(String clusterId) {
    	return clusterConfiguration.getRedisService(clusterId);
    }

    private long getUnlockTime() {
        return 1500L;
    }
}
