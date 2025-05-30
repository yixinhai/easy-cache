package com.xh.easy.easycache.handler;

import com.xh.easy.easycache.async.FunctionAsyncTask;
import com.xh.easy.easycache.context.CacheInfo;
import com.xh.easy.easycache.context.QueryContext;
import com.xh.easy.easycache.context.UpdateContext;
import com.xh.easy.easycache.lock.RedissonLockService;
import com.xh.easy.easycache.risk.event.UpdateFailed;
import com.xh.easy.easycache.risk.event.UpdateSuccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class CacheExecutor {

    @Autowired
    private RedissonLockService lock;


    public CacheInfo getValue(QueryContext context, CacheHandler cacheHandler) {
        return cacheHandler.getValue(context);
    }

    public void setValueAsync(QueryContext context, CacheHandler cacheHandler) {
        FunctionAsyncTask.getRunAsyncInstance()
                .addTask(() -> {
                    this.setValue(context, cacheHandler);
                }).exec();
    }

    public Object setValue(QueryContext context, CacheHandler cacheHandler) {
        return lock.executeTryLock(context.getKey(), () -> {
            // 再次检查缓存，防止其他线程已经更新了缓存
            CacheInfo info = this.getValue(context, cacheHandler);
            if (Objects.nonNull(info) && info.coherent()) {
                log.info("act=setValue msg=请求命中缓存 cacheInfo={}", info);
                return info.isDefaultNullValue() ? null : info.getValue((context.getResultType()));
            }

            // 未命中缓存，执行目标方法并记录缓存
            try {
                return cacheHandler.setValue(context, context.proceed());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean ping(CacheHandler cacheHandler) {
        return cacheHandler.ping();
    }

//    @ZZMQRetry
    public void invalid(UpdateContext context, CacheHandler cacheHandler) {

        String key = context.getKey();

        Boolean updateResult = lock.executeTryLock(key, () -> {
            try {
                return cacheHandler.invalid(context);
            } catch (Exception e) {
                log.error("act=invalid msg=失效缓存失败 clusterId={} key={}", context.getClusterId(), key);
                return false;
            }
        });

        if (updateResult == null || !updateResult) {
            // TODO 标记重试

            new UpdateFailed(key).accept();
        }

        new UpdateSuccess(key).accept();
    }

}
