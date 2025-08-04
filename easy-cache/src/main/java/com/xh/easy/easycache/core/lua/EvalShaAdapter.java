package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.core.lua.event.NoScriptEvent;
import com.xh.easy.easycache.entity.model.CacheResult;
import com.xh.easy.easycache.utils.Assert;
import io.lettuce.core.RedisNoScriptException;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * lua脚本执行适配器
 *
 * @author yixinhai
 */
@Slf4j
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

        String[] result = null;

        try {
            result = commands.evalsha(LuaSh.getDigest(type), OUTPUT_TYPE, new String[] {key}, args);
        } catch (RedisNoScriptException e) {
            log.warn("{} act=evalsha msg=redis服务器未找到指定lua脚本 type={}", LOG_STR, type);

            // 通过eval方式执行脚本
            result = eval(type, key, args);

            // 重新加载脚本
            new NoScriptEvent(this).accept();
        }

        Assert.notBlank(result[1], "lua脚本执行结果为空");
        return new CacheResult(result[0], result[1]);
    }

    /**
     * 通过eval方式执行脚本
     *
     * @param type  lua脚本类型 {@link LuaSh.LuaShEnum}
     * @param key   缓存key
     * @param args  缓存参数
     * @return 脚本执行结果
     */
    private String[] eval(LuaSh.LuaShEnum type, String key, String... args) {
        return commands.eval(type.getSh(), OUTPUT_TYPE, new String[] {key}, args);
    }
}
