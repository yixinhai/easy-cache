package com.xh.easy.easycache.entity.model;

import com.xh.easy.easycache.core.executor.executor.MultiLevelCacheExecutor;

import java.lang.reflect.Type;

import static com.xh.easy.easycache.entity.constant.LuaExecResult.SUCCESS;

/**
 * 缓存信息核心类
 *
 * @author yixinhai
 */
public class CacheInfo {

    /**
     * 缓存value
     */
    private CacheResult cacheResult;

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
        this(new CacheResult(value, SUCCESS), isL2Cache, cacheExecutor);
    }

    /**
     * 构建缓存信息
     *
     * @param cacheResult 缓存内容
     * @param cacheExecutor 缓存处理器
     */
    public CacheInfo(CacheResult cacheResult, MultiLevelCacheExecutor cacheExecutor) {
        this(cacheResult, false, cacheExecutor);
    }

    /**
     * 构建缓存信息
     *
     * @param cacheResult 缓存内容
     * @param isL2Cache 是否从l2缓存获取信息
     * @param cacheExecutor 缓存处理器
     */
    public CacheInfo(CacheResult cacheResult, boolean isL2Cache, MultiLevelCacheExecutor cacheExecutor) {
        this.cacheResult = cacheResult;
        this.isL2Cache = isL2Cache;
        this.cacheExecutor = cacheExecutor;
    }


    public MultiLevelCacheExecutor getCacheExecutor() {
        return cacheExecutor;
    }


    /**
     * 获取缓存内容
     *
     * @param type 缓存内容目标类型
     */
    public Object getValue(Type type) {
        return cacheResult.getValue(type);
    }

    /**
     * 缓存内容是否为防止缓存穿透默认值
     */
    public boolean isDefaultNullValue() {
        return cacheResult.isDefaultNullValue();
    }

    /**
     * 获取缓存执行结果
     */
    public String getExecResult() {
        return cacheResult.getResultType();
    }
}
