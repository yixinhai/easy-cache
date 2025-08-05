package com.xh.easy.easycache.core.lua;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xh.easy.easycache.entity.constant.CacheHashStructure.*;
import static com.xh.easy.easycache.entity.constant.LuaExecResult.*;

/**
 * Redis缓存lua脚本
 *
 * @author yixinhai
 */
public class LuaSh {

    private static final Map<String, String> SH_DIGEST_MAP = new ConcurrentHashMap<>();

    public static String getDigest(LuaShEnum luaShEnum) {
        return SH_DIGEST_MAP.get(luaShEnum.operation);
    }

    /**
     * 记录lua脚本的digest
     *
     * @param operation 脚本操作 {@link LuaShEnum}
     * @param digest redis服务器生成对应脚本的hash值
     */
    protected static void recordShDigest(String operation, String digest) {
        SH_DIGEST_MAP.put(operation, digest);
    }

    /**
     * lua脚本枚举
     */
    public enum LuaShEnum {

        GET("GET", GET_SH),
        SET("SET", SET_SH),
        UNLOCK("UNLOCK", UNLOCK_SH),
        INVALID("INVALID", INVALID_SH)

        ;

        private final String operation;
        private final String sh;

        LuaShEnum(String operation, String sh) {
            this.operation = operation;
            this.sh = sh;
        }

        public String getOperation() {
            return operation;
        }

        public String getSh() {
            return sh;
        }
    }

    /**
     * 获取缓存值
     */
    private static final String GET_SH =
        "local key = KEYS[1]\n"
            + "local newUnlockTime = ARGV[1]\n"
            + "local owner = ARGV[2]\n"
            + "local currentTime = tonumber(ARGV[3])\n"
            + "local value = redis.call('HGET', key, '" + VALUE + "')\n"
            + "local unlockTime = redis.call('HGET', key, '" + UNLOCK_TIME + "')\n"
            + "local lockOwner = redis.call('HGET', key, '" + OWNER + "')\n"
            + "local lockInfo = redis.call('HGET', key, '" + LOCK_INFO + "')\n"
            + "if unlockTime and currentTime > tonumber(unlockTime) then\n"
            + "    redis.call('HMSET', key, '" + LOCK_INFO + "', 'locked', '" + UNLOCK_TIME + "', 'newUnlockTime', '" + OWNER + "', owner)\n"
            + "    return {value, '" + NEED_QUERY + "'}\n"
            + "end\n"
            + "if not value or value == '' then\n"
            + "    if lockOwner and lockOwner ~= owner then\n"
            + "        return {value, '" + NEED_WAIT + "'}\n"
            + "    end\n"
            + "    redis.call('HMSET', key, '" + LOCK_INFO + "', 'locked', '" + UNLOCK_TIME + "', newUnlockTime, '" + OWNER + "', owner)\n"
            + "    return {value, '" + NEED_QUERY + "'}\n"
            + "end\n"
            + "if lockInfo and lockInfo == 'locked' then \n"
            + "    return {value, '" + SUCCESS_NEED_QUERY + "'}\n"
            + "end\n"
            + "return {value , '" + SUCCESS + "'}";

    /**
     * 设置缓存值
     */
    private static final String SET_SH =
        "local key = KEYS[1]\n"
            + "local newValue = ARGV[1]\n"
            + "local submitOwner = ARGV[2]\n"
            + "local expireTime = tonumber(ARGV[3])\n"
            + "local lockOwner = redis.call('HGET', key, '" + OWNER + "')\n"
            + "if not lockOwner or (lockOwner and lockOwner ~= submitOwner) then\n"
            + "    return {false, '" + OWNER_MISMATCH + "'}\n"
            + "end\n"
            + "redis.call('HSET', key, '" + VALUE + "', newValue)\n"
            + "redis.call('HDEL', key, '" + UNLOCK_TIME + "', '" + OWNER + "')\n"
            + "redis.call('HSET', key, '" + LOCK_INFO + "', 'unLock')\n"
            + "if expireTime > 0 then\n"
            + "    redis.call('EXPIRE', key, expireTime)\n"
            + "end\n"
            + "return {'', '" + SUCCESS + "'}";

    /**
     * 解锁缓存
     */
    private static final String UNLOCK_SH =
        "local key = KEYS[1]\n"
            + "local submitOwner = ARGV[1]\n"
            + "local expireTime = tonumber(ARGV[2])\n"
            + "local lockOwner = redis.call('HGET', key, '" + OWNER + "')\n"
            + "if not lockOwner and lockOwner == submitOwner then\n"
            + "   redis.call('HDEL', key, '" + OWNER + "')\n"
            + "end\n"
            + "if expireTime > 0 then\n"
            + "    redis.call('EXPIRE', key, expireTime)\n"
            + "end\n"
            + "return {'', '" + SUCCESS + "'}";

    /**
     * 失效缓存
     */
    private static final String INVALID_SH =
        "local key = KEYS[1]\n"
            + "local newUnlockTime = tonumber(ARGV[1])\n"
            + "redis.call('HDEL', key, '" + OWNER + "')\n"
            + "local value = redis.call('HGET', key, '" + VALUE + "')\n"
            + "redis.call('HSET', key, '" + LOCK_INFO + "', 'locked')\n"
            + "if not value or value == '' then\n" 
            + "    return {true, '" + EMPTY_VALUE_SUCCESS + "'}\n"
            + "end\n"
            + "if newUnlockTime > 0 then\n"
            + "    redis.call('HSET', key, '" + UNLOCK_TIME + "', newUnlockTime)\n"
            + "end\n"
            + "return {'', '" + SUCCESS + "'}";
}