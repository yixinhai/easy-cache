package com.xh.easy.easycache.risk.event;

import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.risk.CacheVisitor;
import com.xh.easy.easycache.risk.healthy.FaultDynamicManager;

/**
 * 集群异常事件
 *
 * @author yixinhai
 */
public class ClusterFault implements Operation {

    private final String clusterId;
    private final CacheVisitor visitor;

    public ClusterFault(String clusterId) {
        this.clusterId = clusterId;
        this.visitor = ClassHandler.getInstance().getBeanByType(FaultDynamicManager.class);
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
