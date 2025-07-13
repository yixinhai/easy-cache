package com.xh.easy.easycache.core.executor;

/**
 * 缓存健康度检查
 *
 * @author yixinhai
 */
public interface CacheHealthyChecker {

    /**
     * 缓存是否可用
     *
     * @param clusterId 集群ID
     * @return 缓存是否可用
     */
    boolean ping(String clusterId);
}
