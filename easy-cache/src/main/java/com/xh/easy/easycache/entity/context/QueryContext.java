package com.xh.easy.easycache.entity.context;

import com.xh.easy.easycache.aop.CacheAble;
import com.xh.easy.easycache.core.RedisKeyParser;
import com.xh.easy.easycache.base.JoinPointContext;
import com.xh.easy.easycache.entity.model.TimeInfo;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 查询缓存上下文
 *
 * @author yixinhai
 */
public class QueryContext extends JoinPointContext implements CacheContext {

    /**
     * 解析redis key
     */
    private final RedisKeyParser redisKeyParser;

    /**
     * 售后redis缓存注解
     */
    private final CacheAble redisCache;

    /**
     * redis 解析key
     */
    private String key;

    /**
     * redis 集群id
     */
    private final String clusterId;

    public QueryContext(ProceedingJoinPoint pjp) {
        super(pjp);

        this.redisCache = this.getAnnotation(CacheAble.class);
        this.redisKeyParser = new RedisKeyParser(this.redisCache.prefix(), this.redisCache.keys(),
                this.getMethod(), pjp.getArgs());
        this.clusterId = this.redisCache.clusterId();
    }

    @Override
    public String getClusterId() {
        return this.clusterId;
    }

    /**
     * 获取redis key
     */
    @Override
    public String getKey() {
        if (null != key && key.length() > 0) {
            return key;
        }
        key = redisKeyParser.parseKey();
        return key;
    }

    /**
     * 是否开启缓存穿透
     */
    public boolean cachePenetration() {
        return this.redisCache.preventCachePenetration();
    }

    /**
     * 缓存过期时间
     */
    public TimeInfo getTimeInfo() {
        return new TimeInfo(redisCache.expireTime(), redisCache.timeUnit());
    }

    /**
     * 是否开启二级缓存
     */
    public boolean enableL2Cache() {
        return redisCache.l2Cache();
    }

    /**
     * 二级缓存比例
     */
    public int getL2CacheProportion() {
        return redisCache.l2CacheProportion();
    }
}
