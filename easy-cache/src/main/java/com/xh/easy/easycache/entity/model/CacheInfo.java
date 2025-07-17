package com.xh.easy.easycache.entity.model;

import com.xh.easy.easycache.core.executor.executor.MultiLevelCacheExecutor;
import com.xh.easy.easycache.utils.serialze.SerializerManager;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
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

    public static final String NULL = "easy-cache-default-null";

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

    /**
     * 缓存处理器
     */
    private MultiLevelCacheExecutor cacheExecutor;


    public CacheInfo() {
    }

    /**
     * 构建缓存信息
     *
     * @param value 缓存内容
     * @param isL2Cache 是否从l2缓存获取信息
     * @param cacheExecutor 缓存处理器
     */
    public CacheInfo(String value, Boolean isL2Cache, MultiLevelCacheExecutor cacheExecutor) {
        this(value, null, null, isL2Cache, cacheExecutor);
    }

    /**
     * 构建缓存信息
     *
     * @param value 缓存内容
     * @param lockInfo 缓存内容状态
     * @param cacheExecutor 缓存处理器
     */
    public CacheInfo(String value, String lockInfo, MultiLevelCacheExecutor cacheExecutor) {
        this(value, lockInfo, null, false, cacheExecutor);
    }

    /**
     * 构建缓存信息
     *
     * @param value 缓存内容
     * @param lockInfo 缓存内容状态
     * @param unlockTime 缓存内容延迟删除截止时间
     * @param isL2Cache 是否从l2缓存获取信息
     * @param cacheExecutor 缓存处理器
     */
    public CacheInfo(String value, String lockInfo, String unlockTime, boolean isL2Cache,
        MultiLevelCacheExecutor cacheExecutor) {

        this.value = value;
        this.lockInfo = lockInfo;
        this.unlockTime = unlockTime;
        this.isL2Cache = isL2Cache;
        this.cacheExecutor = cacheExecutor;
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

    public MultiLevelCacheExecutor getCacheExecutor() {
        return cacheExecutor;
    }

    public void setCacheExecutor(MultiLevelCacheExecutor cacheExecutor) {
        this.cacheExecutor = cacheExecutor;
    }

    /**
     * 是否从l2缓存中获取的返回值
     */
    public boolean isL2Cache() {
        return isL2Cache;
    }

    /**
     * 将cacheInfo映射成map
     */
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

    /**
     * 将map映射成cacheInfo
     */
    public static CacheInfo initByCacheMap(Map<String, String> map, MultiLevelCacheExecutor cacheExecutor) {
        CacheInfo info = new CacheInfo();
        if (null == map || map.isEmpty()) {
            return info;
        }
        info.setValue(map.get(HASH_FIELD_VALUE));
        info.setLockInfo(map.get(HASH_FIELD_LOCK_INFO));
        info.setUnlockTime(map.get(HASH_FIELD_UNLOCK_TIME));
        info.setCacheExecutor(cacheExecutor);
        return info;
    }

    /**
     * 获取缓存内容
     *
     * @param type 缓存内容目标类型
     */
    public Object getValue(Type type) {

        // 开启防止缓存穿透时，value可能为字符串"null"
        if (isDefaultNullValue()) {
            return null;
        }

        return SerializerManager.jsonSerializer().deserialize(value, type);
    }

    /**
     * 缓存内容是否为防止缓存穿透默认值
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
    public boolean lockTimeout(final long elasticExpirationTime) {
        Long unLockTime = getUnlockTime();

        if (unLockTime == null) {
            return false;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long startTime = unLockTime - elasticExpirationTime;

        return currentTimeMillis <= unLockTime && currentTimeMillis > startTime;
    }

    /**
     * 是否命中缓存
     */
    public boolean hit() {
        return value != null && !value.isEmpty();
    }
}
