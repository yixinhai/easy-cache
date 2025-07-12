package com.xh.easy.easycache.risk.event;

import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.risk.CacheVisitor;
import com.xh.easy.easycache.risk.healthy.FaultDynamicManager;

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
        this.visitor = ClassHandler.getInstance().getBeanByType(FaultDynamicManager.class);
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
