package com.xh.easy.easycache.core.executor;

import com.xh.easy.easycache.entity.context.UpdateContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.utils.serialze.SerializerManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存执行器
 *
 * @author yixinhai
 */
@Slf4j
public abstract class MultiLevelCacheExecutor implements CacheExecutor {

    @Override
    public Object hit(QueryContext context, CacheInfo info) {
        String value = info.getValue();

        if (value == null || value.isEmpty()) {
            return null;
        }

        return SerializerManager.jsonSerializer().deserialize(value, context.getResultType());
    }

    @Override
    public Object miss(QueryContext context) {
        // 未命中缓存，执行目标方法并记录缓存
        try {
            return setValue(context, context.proceed());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lockCacheInfo(UpdateContext context) {
        CacheBuilder.getAllHandler().forEach(handler -> invalid(context));
    }

    /**
     * 设置缓存
     *
     * @param context 缓存上下文
     * @param o 目标方法返回值
     * @return 目标方法返回值
     */
    public abstract Object setValue(QueryContext context, Object o);

    /**
     * 缓存是否可用
     *
     * @param clusterId 集群ID
     * @return 缓存是否可用
     */
    public abstract boolean ping(String clusterId);

    /**
     * 获取缓存内容
     *
     * @param context 缓存上下文
     * @return 缓存信息
     */
    public abstract CacheInfo loadValue(QueryContext context);

}
