package com.xh.easy.easycache.core.monitor.healthy.event;

import com.xh.easy.easycache.core.executor.executor.CacheExecutor;
import com.xh.easy.easycache.core.monitor.healthy.CacheVisitor;
import com.xh.easy.easycache.core.monitor.healthy.FaultDynamicManager;

/**
 * 集群异常事件
 *
 * @author yixinhai
 */
public class ClusterFault extends CacheOperation {

    private final String clusterId;
    private final CacheVisitor visitor;

    public ClusterFault(String clusterId, CacheExecutor cacheExecutor) {
        super(cacheExecutor);
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
