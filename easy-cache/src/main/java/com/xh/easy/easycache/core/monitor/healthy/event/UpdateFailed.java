package com.xh.easy.easycache.core.monitor.healthy.event;

import com.xh.easy.easycache.core.monitor.healthy.CacheVisitor;
import com.xh.easy.easycache.core.monitor.healthy.FaultDynamicManager;

/**
 * 缓存更新异常事件
 *
 * @author yixinhai
 */
public class UpdateFailed implements Operation {

    private final String key;
    private final CacheVisitor visitor;

    public String getKey() {
        return key;
    }

    public UpdateFailed(String key) {
        this.key = key;
        this.visitor = FaultDynamicManager.getInstance();
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
