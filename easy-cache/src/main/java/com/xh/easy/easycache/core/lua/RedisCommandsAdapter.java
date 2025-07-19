package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.entity.model.CacheResult;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * 缓存命令执行适配器
 *
 * @author yixinhai
 */
public abstract class RedisCommandsAdapter {

    /**
     * 适配对象：redis命令执行器
     */
    protected RedisCommands<String, String> commands;

    /**
     * 构造函数
     *
     * @param commands redis命令执行器
     */
    protected RedisCommandsAdapter(RedisCommands<String, String> commands) {
        this.commands = commands;
    }

    /**
     * 获取锁并获取缓存数据
     *
     * @param key          缓存key
     * @param unlockTime   有效时间
     * @param owner        锁拥有者
     * @param currentTime  当前时间
     * @return 缓存数据
     */
    public abstract CacheResult getAndLock(String key, long unlockTime, String owner, long currentTime);

    /**
     * 解锁缓存
     *
     * @param key    缓存key
     * @param owner  锁拥有者
     * @return 解锁结果
     */
    public CacheResult unlock(String key, String owner) {
        return unlock(key, owner, 0);
    }

    /**
     * 解锁缓存
     *
     * @param key          缓存key
     * @param owner        锁拥有者
     * @param currentTime  当前时间
     * @return 解锁结果
     */
    public abstract CacheResult unlock(String key, String owner, long currentTime);

    /**
     * 设置缓存
     *
     * @param key          缓存key
     * @param value        缓存值
     * @param owner        锁拥有者
     * @return 设置结果
     */
    public CacheResult set(String key, String value, String owner) {
        return set(key, value, owner, 0);
    };

    /**
     * 设置缓存
     *
     * @param key          缓存key
     * @param value        缓存值
     * @param owner        锁拥有者
     * @param expireTime   过期时间
     * @return 设置结果
     */
    public abstract CacheResult set(String key, String value, String owner, long expireTime);

    /**
     * 失效缓存
     *
     * @param key          缓存key
     * @param unlockTime   有效时间
     * @return 删除结果
     */
    public abstract CacheResult invalid(String key, long unlockTime);

    /**
     * 缓存集群探活
     *
     * @return 探活结果
     */
    public boolean ping() {
        return "PONG".equals(commands.ping());
    }
}
