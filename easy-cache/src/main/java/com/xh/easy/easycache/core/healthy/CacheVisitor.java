package com.xh.easy.easycache.core.healthy;

import com.xh.easy.easycache.core.healthy.event.ClusterFault;
import com.xh.easy.easycache.core.healthy.event.UpdateFailed;
import com.xh.easy.easycache.core.healthy.event.UpdateSuccess;

/**
 * 缓存操作观察者
 *
 * @author yixinhai
 */
public interface CacheVisitor {

    /**
     * 更新失败
     */
    void visit(UpdateFailed element);

    /**
     * 更新成功
     */
    void visit(UpdateSuccess element);

    /**
     * 集群故障
     */
    void visit(ClusterFault element);
}
