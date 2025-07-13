package com.xh.easy.easycache.entity.context;

import com.xh.easy.easycache.aop.CacheUpdate;
import com.xh.easy.easycache.utils.RedisKeyParser;
import com.xh.easy.easycache.base.JoinPointContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 更新缓存上下文
 *
 * @author yixinhai
 */
public class UpdateContext extends JoinPointContext implements CacheContext {

    private final CacheUpdate cacheUpdate;

    private final RedisKeyParser redisKeyParser;

    private String key;

    public UpdateContext(ProceedingJoinPoint pjp) {
        super(pjp);
        this.cacheUpdate = getAnnotation(CacheUpdate.class);
        this.redisKeyParser = new RedisKeyParser(cacheUpdate.prefix(), cacheUpdate.keys(), getMethod(), getArgs());
    }

    @Override
    public String getKey() {
        if (null != key && key.length() > 0) {
            return key;
        }
        key = redisKeyParser.parseKey();
        return key;
    }

    @Override
    public String getClusterId() {
        return cacheUpdate.clusterId();
    }
}
