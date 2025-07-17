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


}
