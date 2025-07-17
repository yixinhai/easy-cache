package com.xh.easy.easycache.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import static com.xh.easy.easycache.entity.constant.CacheConfigConstant.*;

/**
 * 缓存注解
 * @author yixinhai
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheAble {

    /**
     * 缓存集群id
     */
    String clusterId();

    /**
     * key前缀
     */
    String prefix();

    /**
     * key内容，多个key之间"_"连接
     */
    String[] keys() default {};

    /**
     * 缓存时间
     * 默认单位：秒
     * 默认缓存时常：10分钟
     */
    long expireTime() default DEFAULT_EXPIRE_TIME;

    /**
     * 缓存时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否开启二级缓存
     * 当redis宕机时，将50%的请求结果缓存至本地，默认缓存2分钟
     */
    boolean l2Cache() default false;

    /**
     * 二级缓存请求比例
     * 5=50%（最大值为10）
     * 当redis宕机时，将指定比例的请求结果缓存至本地，默认缓存2分钟
     */
    int l2CacheProportion() default 5;

    /**
     * 是否开启防止缓存穿透
     */
    boolean preventCachePenetration() default false;

    /**
     * 弹性缓存过期时间【当前时间窗口内缓存数据库不一致】
     * 默认单位：毫秒
     * 默认缓存时间弹性：1500毫秒
     */
    long elasticExpirationTime() default DEFAULT_ELASTIC_CACHE_EXPIRATION_TIME;
}
