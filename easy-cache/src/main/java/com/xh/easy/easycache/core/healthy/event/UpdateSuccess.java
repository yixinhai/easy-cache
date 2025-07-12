package com.xh.easy.easycache.core.healthy.event;

import com.xh.easy.easycache.core.healthy.CacheVisitor;
import com.xh.easy.easycache.core.healthy.FaultDynamicManager;

/**
 * 缓存更新成功事件
 *
 * @author yixinhai
 */
public class UpdateSuccess implements Operation {

    private final String key;
    private final CacheVisitor visitor;

    public String getKey() {
        return key;
    }

    public UpdateSuccess(String key) {
        this.key = key;
        this.visitor = FaultDynamicManager.getInstance();
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
