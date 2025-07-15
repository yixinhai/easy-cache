package com.xh.easy.easycache.core.executor.handler;

import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.core.executor.executor.MultiLevelCacheExecutor;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.core.monitor.healthy.event.ClusterFault;
import lombok.extern.slf4j.Slf4j;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 缓存执行链
 *
 * @author yixinhai
 */
@Slf4j
public abstract class CacheChain extends MultiLevelCacheExecutor {

    /**
     * 下一个缓存执行器
     */
    protected CacheChain next;

    /**
     * 设置下一个缓存执行器
     *
     * @param chain 缓存执行器
     */
    protected final void setNext(CacheChain chain) {
        this.next = chain;
    }

    /**
     * 当前缓存执行器是否可执行
     *
     * @param context 缓存执行上下文
     * @return true 支持
     */
    protected abstract boolean support(QueryContext context);

    @Override
    public CacheInfo loadValue(QueryContext context) {

        if (support(context)) {
            try {
                return getValue(context);
            } catch (Exception e) {
                log.warn("{} act=loadValue msg=当前缓存级别获取数据异常 class={} key={}", LOG_STR,
                    ClassHandler.getClassName(this), context.getKey(), e);

                // 更新故障信息
                new ClusterFault(context.getClusterId(), this).accept();

                return nextLoadValue(context);
            }
        }

        return nextLoadValue(context);
    }

    /**
     * 交给下一个执行器执行
     *
     * @param context 缓存执行上下文
     * @return 缓存数据
     */
    private CacheInfo nextLoadValue(QueryContext context) {
        return next != null ? next.loadValue(context) : null;
    }
}
