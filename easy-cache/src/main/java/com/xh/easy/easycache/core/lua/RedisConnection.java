package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.entity.model.CacheResult;

/**
 * Redis集群连接
 *
 * @author yixinhai
 */
public interface RedisConnection {

    /**
     * 获取锁并获取缓存数据
     *
     * @param key          缓存key
     * @param unlockTime   有效时间
     * @param owner        锁拥有者
     * @param currentTime  当前时间
     * @return 缓存数据
     */
    CacheResult getAndLock(String key, long unlockTime, String owner, long currentTime);

    /**
     * 解锁缓存
     *
     * @param key    缓存key
     * @param owner  锁拥有者
     * @return 解锁结果
     */
    CacheResult unlock(String key, String owner);

    /**
     * 解锁缓存
     *
     * @param key          缓存key
     * @param owner        锁拥有者
     * @param currentTime  当前时间
     * @return 解锁结果
     */
    CacheResult unlock(String key, String owner, long currentTime);

    /**
     * 设置缓存
     *
     * @param key          缓存key
     * @param value        缓存值
     * @param owner        锁拥有者
     * @return 设置结果
     */
    CacheResult set(String key, String value, String owner);

    /**
     * 设置缓存
     *
     * @param key          缓存key
     * @param value        缓存值
     * @param owner        锁拥有者
     * @param expireTime   过期时间
     * @return 设置结果
     */
    CacheResult set(String key, String value, String owner, long expireTime);

    /**
     * 失效缓存
     *
     * @param key          缓存key
     * @param unlockTime   有效时间
     * @return 删除结果
     */
    CacheResult invalid(String key, long unlockTime);

    /**
     * 缓存集群探活
     *
     * @return 探活结果
     */
    boolean ping();

    /**
     * 将lua脚本加载到redis服务器
     *
     * @param luaScript lua脚本
     */
    String scriptLoad(String luaScript);
}
