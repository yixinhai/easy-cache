package com.xh.easy.easycache.core.monitor.healthy.event;

import com.xh.easy.easycache.core.executor.executor.CacheExecutor;
import com.xh.easy.easycache.core.monitor.healthy.CacheVisitor;
import com.xh.easy.easycache.core.monitor.healthy.FaultDynamicManager;

/**
 * 缓存更新异常事件
 *
 * @author yixinhai
 */
public class UpdateFailed extends CacheOperation {

    private final String key;
    private final CacheVisitor visitor;

    public String getKey() {
        return key;
    }

    public UpdateFailed(String key, CacheExecutor executor) {
        super(executor);
        this.key = key;
        this.visitor = FaultDynamicManager.getInstance();
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
