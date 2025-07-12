package com.xh.easy.easycache.handler;

import com.alibaba.fastjson.JSON;
import com.xh.easy.easycache.ClusterConfiguration;
import com.xh.easy.easycache.context.CacheContext;
import com.xh.easy.easycache.context.CacheInfo;
import com.xh.easy.easycache.context.QueryContext;
import com.xh.easy.easycache.context.TimeInfo;
import com.xh.easy.easycache.risk.healthy.ClusterHealthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.xh.easy.easycache.context.CacheInfo.*;

/**
 * 集群缓存处理器
 *
 * @author yixinhai
 */
@Slf4j
@Component(value = "redisCache")
public class RedisCache extends CacheChain {

    @Autowired
    private ClusterConfiguration clusterConfiguration;

    @Autowired
    private ClusterHealthInfo clusterHealthInfo;


    @Override
    public CacheInfo getValue(QueryContext context) {
        return CacheInfo.initByCacheMap(getService(context).hgetall(context.getKey()));
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
//        return getService(clusterId).ping(redisCache);
        return true;
    };

    @Override
    public boolean invalid(CacheContext context) {
        CacheInfo info = new CacheInfo();
        info.setLockInfo(LOCKED);
        info.setUnlockTime(String.valueOf(System.currentTimeMillis() + 1500));
        return "OK".equals(getService(context).hsetall(context.getKey(), info.parseCacheMap()));
    }

    @Override
    public boolean support(QueryContext context) {
        return clusterHealthInfo.isClusterAvailable(context.getKey(), context.getClusterId());
    }

    @Override
    public Object loadValue(QueryContext context) {
        return getValue(context);
    }

    private Map<String, String> convertValue(QueryContext context, Object o) {
        Object cast = context.getResultType().cast(o);
        String fieldValue = cast == null ? NULL : JSON.toJSONString(cast);
        return new CacheInfo(fieldValue, UN_LOCK).parseCacheMap();
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
}
