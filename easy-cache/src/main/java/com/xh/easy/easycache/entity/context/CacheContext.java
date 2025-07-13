package com.xh.easy.easycache.entity.context;

/**
 * 缓存上下文
 *
 * @author yixinhai
 */
public interface CacheContext {

    /**
     * 获取缓存key
     *
     * @return 缓存key
     */
    String getKey();

    /**
     * 获取缓存集群id
     *
     * @return 缓存集群id
     */
    String getClusterId();
}
