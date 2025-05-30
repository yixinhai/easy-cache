package com.xh.easy.easycache.context;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 缓存信息核心类
 *
 * @author yixinhai
 */
public class CacheInfo {

    public static final String NULL = "null";

    /**
     * 缓存内容状态：说明此时缓存与db一致，可以直接返回value
     */
    public static final String UN_LOCK = "unLock";

    /**
     * 缓存内容状态：说明此时db已经变更
     */
    public static final String LOCKED = "locked";

    /**
     * 缓存hash结构：实际缓存内容
     */
    private static final String HASH_FIELD_VALUE = "value";

    /**
     * 缓存hash结构：缓存内容状态
     */
    public static final String HASH_FIELD_LOCK_INFO = "lockInfo";

    /**
     * 缓存hash结构：延迟删除截止时间
     */
    public static final String HASH_FIELD_UNLOCK_TIME = "unLockTime";

    /**
     * 缓存hash结构：实际缓存内容
     */
    private String value;

    /**
     * 缓存hash结构：缓存内容状态
     */
    private String lockInfo;

    /**
     * 缓存hash结构：延迟删除截止时间
     * 即：当前时间戳之前命中缓存时，及时缓存已经是更新状态，仍然返回缓存value
     */
    private String unlockTime;

    /**
     * 是否从l2缓存中获取的返回值
     */
    private boolean isL2Cache;

    public CacheInfo() {
    }

    public CacheInfo(String value, Boolean isL2Cache) {
        this(value, null, null, isL2Cache);
    }

    public CacheInfo(String value, String lockInfo) {
        this(value, lockInfo, null, false);
    }

    public CacheInfo(String value, String lockInfo, String unlockTime, boolean isL2Cache) {
        this.value = value;
        this.lockInfo = lockInfo;
        this.unlockTime = unlockTime;
        this.isL2Cache = isL2Cache;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(String lockInfo) {
        this.lockInfo = lockInfo;
    }

    public Long getUnlockTime() {
        return Optional.ofNullable(unlockTime).map(Long::valueOf).orElse(null);
    }

    public void setUnlockTime(String unlockTime) {
        this.unlockTime = unlockTime;
    }

    /**
     * 是否从l2缓存中获取的返回值
     */
    public boolean isL2Cache() {
        return isL2Cache;
    }

    public void setL2Cache(boolean l2Cache) {
        isL2Cache = l2Cache;
    }

    public Map<String, String> parseCacheMap() {
        Map<String, String> result = new HashMap<>(3);
        if (StringUtils.hasText(value)) {
            result.put(HASH_FIELD_VALUE, value);
        }
        if (StringUtils.hasText(lockInfo)) {
            result.put(HASH_FIELD_LOCK_INFO, lockInfo);
        }
        if (StringUtils.hasText(unlockTime)) {
            result.put(HASH_FIELD_UNLOCK_TIME, unlockTime);
        }
        return result;
    }

    public static CacheInfo initByCacheMap(Map<String, String> map) {
        CacheInfo info = new CacheInfo();
        if (null == map || map.isEmpty()) {
            return info;
        }
        info.setValue(map.get(HASH_FIELD_VALUE));
        info.setLockInfo(map.get(HASH_FIELD_LOCK_INFO));
        info.setUnlockTime(map.get(HASH_FIELD_UNLOCK_TIME));
        return info;
    }

    public <R> R getValue(Class<R> resultType) {
        // 开启防止缓存穿透时，value可能为字符串"null"
        if (NULL.equals(value)) {
            return null;
        }
        return JSON.parseObject(value, resultType);
    }

    /**
     * 缓存内容为防止缓存穿透默认值
     */
    public boolean isDefaultNullValue() {
        return NULL.equals(value);
    }

    /**
     * 缓存内容与数据库是否一致
     */
    public boolean coherent() {
        return UN_LOCK.equals(lockInfo);
    }

    /**
     * 缓存内容是否已过期
     */
    public boolean lockTimeout() {
        Long unLockTime = getUnlockTime();
        return Objects.nonNull(unLockTime) && System.currentTimeMillis() <= unLockTime;
    }
}
