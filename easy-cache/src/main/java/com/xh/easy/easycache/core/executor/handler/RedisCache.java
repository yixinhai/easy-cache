package com.xh.easy.easycache.core.executor.handler;

import com.xh.easy.easycache.base.ApplicationContextAdapter;
import com.xh.easy.easycache.core.lua.RedisCommandsAdapter;
import com.xh.easy.easycache.core.lua.RedisCommandsManager;
import com.xh.easy.easycache.core.monitor.healthy.event.ClusterFault;
import com.xh.easy.easycache.entity.context.CacheContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.model.TimeInfo;
import com.xh.easy.easycache.core.monitor.healthy.ClusterHealthInfo;
import com.xh.easy.easycache.utils.serialze.SerializerManager;
import lombok.extern.slf4j.Slf4j;

import static com.xh.easy.easycache.entity.constant.CacheConfigConstant.GET_VALUE_UNLOCK_TIME;
import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 集群缓存处理器
 *
 * @author yixinhai
 */
@Slf4j
public class RedisCache extends CacheChain {

    private static final RedisCache INSTANCE = new RedisCache();

    private final RedisCommandsManager redisCommandsManager;

    private RedisCache() {
        this.redisCommandsManager = ApplicationContextAdapter.getBeanByType(RedisCommandsManager.class);
    }

    public static RedisCache getInstance() {
        return INSTANCE;
    }

    @Override
    public CacheInfo getValue(QueryContext context) {
        long currentTimeMillis = System.currentTimeMillis();
        long unlockTime = currentTimeMillis + GET_VALUE_UNLOCK_TIME;

        return new CacheInfo(
            getCommands(context).getAndLock(context.getKey(), unlockTime, context.getUuid(), currentTimeMillis), this);
    }

    @Override
    public Object setValue(QueryContext context, Object o) {

        String key = context.getKey();
        String uuid = context.getUuid();

        try {
            // 缓存信息
            TimeInfo timeInfo = context.getTimeInfo();
            String value = this.convertValue(o);

            if (timeInfo.getExpireTime() > 0) {
                getCommands(context).set(key, value, uuid, timeInfo.toSeconds());
            } else {
                getCommands(context).set(key, value, uuid);
            }

        } catch (Exception e) {
            log.warn("{} act=setValue msg=Redis缓存写入失败 key={}", LOG_STR, key, e);
            new ClusterFault(context.getClusterId(), this);
        }

        return o;
    }

    @Override
    public boolean ping(String clusterId) {
        return getCommands(clusterId).ping();
    }

    @Override
    public boolean invalid(CacheContext context) {
        return getCommands(context).invalid(context.getKey(), ((QueryContext)context).getElasticExpirationTime())
            .invalidSuccess();
    }

    @Override
    public boolean support(QueryContext context) {
        return ClusterHealthInfo.isClusterAvailable(context.getKey(), context.getClusterId());
    }

    /**
     * 将目标方法返回结果转换成缓存信息
     *
     * @param o 目标方法返回结果
     * @return map格式缓存信息
     */
    private String convertValue(Object o) {
        return o == null ? null : SerializerManager.jsonSerializer().serialize2String(o);
    }

    /**
     * 获取Redis命令处理器
     *
     * @param context 缓存上下文
     */
    private RedisCommandsAdapter getCommands(CacheContext context) {
    	return getCommands(context.getClusterId());
    }

    /**
     * 获取Redis命令处理器
     *
     * @param clusterId 集群ID
     */
    private RedisCommandsAdapter getCommands(String clusterId) {
    	return redisCommandsManager.getRedisCommands(clusterId);
    }
}
