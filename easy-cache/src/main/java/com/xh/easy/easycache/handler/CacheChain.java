package com.xh.easy.easycache.handler;

import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.context.QueryContext;
import com.xh.easy.easycache.risk.event.ClusterFault;
import lombok.extern.slf4j.Slf4j;

import static com.xh.easy.easycache.base.LogStrConstant.LOG_STR;

/**
 * 缓存执行链
 *
 * @author yixinhai
 */
@Slf4j
public abstract class CacheChain extends CacheHandler {

    /**
     * 下一个缓存执行器
     */
    protected CacheChain next;

    /**
     * 设置下一个缓存执行器
     *
     * @param chain 缓存执行器
     */
    protected void setNext(CacheChain chain) {
        this.next = chain;
    }

    /**
     * 当前缓存执行器是否可执行
     *
     * @param context 缓存执行上下文
     * @return true 支持
     */
    public abstract boolean support(QueryContext context);

    /**
     * 获取缓存数据
     *
     * @param context 缓存执行上下文
     * @return 缓存数据
     */
    public Object loadValue(QueryContext context) {

        if (support(context)) {
            try {
                return getValue(context);
            } catch (Exception e) {
                log.warn("{} act=loadValue msg=当前缓存级别获取数据异常 class={} key={}", LOG_STR,
                    ClassHandler.getClassName(this), context.getKey(), e);

                // 更新故障信息
                new ClusterFault(context.getClusterId()).accept();

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
    private Object nextLoadValue(QueryContext context) {
        return next != null ? next.loadValue(context) : null;
    }
}
