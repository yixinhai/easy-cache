package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.entity.model.CacheResult;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * 缓存命令执行适配器
 *
 * @author yixinhai
 */
public abstract class RedisCommandsAdapter implements RedisConnection {

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

    @Override
    public CacheResult unlock(String key, String owner) {
        return unlock(key, owner, 0);
    }

    @Override
    public CacheResult set(String key, String value, String owner) {
        return set(key, value, owner, 0);
    };

    @Override
    public boolean ping() {
        return "PONG".equals(commands.ping());
    }

    @Override
    public String scriptLoad(String luaScript) {
        return commands.scriptLoad(luaScript);
    }
}
