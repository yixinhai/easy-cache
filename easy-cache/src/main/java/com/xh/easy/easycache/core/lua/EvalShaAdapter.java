package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.entity.model.CacheResult;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * lua脚本执行适配器
 *
 * @author yixinhai
 */
public class EvalShaAdapter extends RedisCommandsAdapter {

    private static final ScriptOutputType OUTPUT_TYPE = ScriptOutputType.MULTI;

    private EvalShaAdapter(RedisCommands<String, String> commands) {
        super(commands);
    }

    /**
     * 创建适配器
     *
     * @param commands redis命令执行类
     * @return 适配器
     */
    public static EvalShaAdapter of(RedisCommands<String, String> commands) {
        return new EvalShaAdapter(commands);
    }

    @Override
    public CacheResult getAndLock(String key, long unlockTime, String owner, long currentTime) {
        return getAndLock(key, String.valueOf(unlockTime), owner, String.valueOf(currentTime));
    }

    @Override
    public CacheResult unlock(String key, String owner, long currentTime) {
        return unlock(key, owner, String.valueOf(currentTime));
    }

    @Override
    public CacheResult set(String key, String value, String owner, long expireTime) {
        return set(key, value, owner, String.valueOf(expireTime));
    }

    @Override
    public CacheResult invalid(String key, long unlockTime) {
        return invalid(key, String.valueOf(unlockTime));
    }

    private CacheResult getAndLock(String key, String unlockTime, String owner, String currentTime) {
        return evalsha(LuaSh.LuaShEnum.GET, key, unlockTime, owner, currentTime);
    }

    private CacheResult set(String key, String value, String owner, String expireTime) {
        return evalsha(LuaSh.LuaShEnum.SET, key, value, owner, expireTime);
    }

    private CacheResult invalid(String key, String unlockTime) {
        return evalsha(LuaSh.LuaShEnum.INVALID, key, unlockTime);
    }

    private CacheResult unlock(String key, String owner, String currentTime) {
        return evalsha(LuaSh.LuaShEnum.UNLOCK, key, owner, currentTime);
    }

    /**
     * 执行lua脚本
     *
     * @param type  lua脚本类型 {@link LuaSh.LuaShEnum}
     * @param key   缓存key
     * @param args  缓存参数
     * @return 脚本执行结果
     */
    private CacheResult evalsha(LuaSh.LuaShEnum type, String key, String... args) {
        String[] result = commands.evalsha(LuaSh.getDigest(type), OUTPUT_TYPE, new String[] {key}, args);
        return new CacheResult(result[0], result[1]);
    }
}
