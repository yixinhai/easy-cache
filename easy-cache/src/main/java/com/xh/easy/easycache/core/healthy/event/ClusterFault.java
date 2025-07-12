package com.xh.easy.easycache.core.healthy.event;

import com.xh.easy.easycache.core.healthy.CacheVisitor;
import com.xh.easy.easycache.core.healthy.FaultDynamicManager;

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
        this.visitor = FaultDynamicManager.getInstance();
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}
