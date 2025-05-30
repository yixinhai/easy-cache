package com.xh.easy.easycache;

import com.xh.easy.easycache.base.ResultHandler;
import com.xh.easy.easycache.context.CacheInfo;
import com.xh.easy.easycache.context.QueryContext;
import com.xh.easy.easycache.context.UpdateContext;
import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import com.xh.easy.easycache.risk.FaultTolerance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 缓存调度器
 *
 * @author yixinhai
 */
@Slf4j
@Service
public class CacheDispatcher {

    @Autowired
    private FaultTolerance faultTolerance;


    public Object doDispatch(QueryContext context) {

        String key = context.getKey();

        // 获取缓存内容，redis缓存宕机处理
        CacheInfo info = faultTolerance.safelyGetValue(context);
        log.info("act=CacheDispatcher_doDispatch msg=缓存结果：key={} info={}", key, info);

        if (Objects.isNull(info)) {
            return ResultHandler.defaultResult(context);
        }

        // 处理结果
        String value = info.getValue();
        return StringUtils.hasText(value) ? faultTolerance.hit(context, info) : faultTolerance.miss(context);
    }

    public Object doDispatch(UpdateContext context) {

        String key = context.getKey();

        // TODO 记录本地消息表，启动服务时进行流量回放

        try {
            return context.proceed();
        } catch (TargetMethodExecFailedException e) {
            log.error("act=CacheDispatcher_doDispatch msg=目标方法执行异常 key={}", key, e);
        } finally {
            // 上报缓存更新
            faultTolerance.lockCacheInfo(context);
        }

        return ResultHandler.defaultResult(context);
    }
}
