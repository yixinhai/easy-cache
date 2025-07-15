package com.xh.easy.easycache.core.monitor.healthy.event;

import com.xh.easy.easycache.core.executor.executor.CacheExecutor;
import com.xh.easy.easycache.core.monitor.healthy.CacheVisitor;
import com.xh.easy.easycache.core.monitor.healthy.FaultDynamicManager;

/**
 * 缓存更新成功事件
 *
 * @author yixinhai
 */
public class UpdateSuccess extends CacheOperation {

    private final String key;
    private final CacheVisitor visitor;

    public String getKey() {
        return key;
    }

    public UpdateSuccess(String key, CacheExecutor cacheExecutor) {
        super(cacheExecutor);
        this.key = key;
        this.visitor = FaultDynamicManager.getInstance();
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
