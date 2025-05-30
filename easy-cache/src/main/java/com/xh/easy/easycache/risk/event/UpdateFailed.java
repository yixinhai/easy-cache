package com.xh.easy.easycache.risk.event;

import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.risk.CacheVisitor;
import com.xh.easy.easycache.risk.FaultManager;

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
        this.visitor = ClassHandler.getInstance().getBeanByType(FaultManager.class);
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
