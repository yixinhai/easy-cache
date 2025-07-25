package com.xh.easy.easycache.entity.constant;

/**
 * 缓存配置常量
 *
 * @author yixinhai
 */
public class CacheConfigConstant {

    /**
     * 默认缓存过期时间
     */
    public static final long DEFAULT_EXPIRE_TIME = 600L;

    /**
     * 弹性缓存标记删除时间（当前时间窗口内缓存数据库不一致）
     */
    public static final long DEFAULT_ELASTIC_CACHE_EXPIRATION_TIME = 1500L;

    /**
     * 缓存被锁定时最大重试时间
     */
    public static final long MAX_RETRY_TIME = 1500L;

    /**
     * 缓存被锁定时重试时间间隔
     */
    public static final long RETRY_TIME_INTERVAL = 100L;

    /**
     * 获取缓存值时锁超时时间
     */
    public static final long GET_VALUE_UNLOCK_TIME = 1000L;
}
