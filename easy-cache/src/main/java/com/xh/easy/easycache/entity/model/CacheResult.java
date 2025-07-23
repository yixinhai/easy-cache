package com.xh.easy.easycache.entity.model;

import com.xh.easy.easycache.entity.constant.LuaExecResult;
import com.xh.easy.easycache.utils.serialze.SerializerManager;

import java.lang.reflect.Type;

/**
 * 缓存执行结果
 *
 * @author yixinhai
 */
public class CacheResult {

    public static final String NULL = "easy-cache-default-null";

    /**
     * 缓存hash结构：实际缓存内容
     */
    private final String value;

    private final String resultType;

    public CacheResult(String value, String resultType) {
        this.value = value;
        this.resultType = resultType;
    }

    public String getResultType() {
        return resultType;
    }

    /**
     * 获取缓存内容
     *
     * @param type
     *     缓存内容目标类型
     */
    protected Object getValue(Type type) {
        return value == null || value.isBlank() ? null : SerializerManager.jsonSerializer().deserialize(value, type);
    }

    /**
     * 缓存内容是否为防止缓存穿透默认值
     */
    protected boolean isDefaultNullValue() {
        return NULL.equals(value);
    }

    /**
     * 缓存内容是否为删除缓存成功
     */
    public boolean invalidSuccess() {
        return LuaExecResult.SUCCESS.equals(value);
    }
}
