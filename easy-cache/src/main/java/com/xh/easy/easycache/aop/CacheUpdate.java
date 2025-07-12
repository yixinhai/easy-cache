package com.xh.easy.easycache.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheUpdate {

    /**
     * 集群id
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
}
