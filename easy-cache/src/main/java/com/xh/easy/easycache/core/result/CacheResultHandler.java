package com.xh.easy.easycache.core.result;

import com.xh.easy.easycache.core.executor.executor.CacheExecutor;
import com.xh.easy.easycache.entity.context.QueryContext;
import com.xh.easy.easycache.entity.model.CacheInfo;
import com.xh.easy.easycache.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.xh.easy.easycache.entity.constant.CacheConfigConstant.MAX_RETRY_TIME;
import static com.xh.easy.easycache.entity.constant.CacheConfigConstant.RETRY_TIME_INTERVAL;
import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;
import static com.xh.easy.easycache.entity.constant.LuaExecResult.*;
import static com.xh.easy.easycache.entity.constant.LuaExecResult.NEED_WAIT;

/**
 * 缓存结果处理器
 *
 * @author yixinhai
 */
@Slf4j
@Component
public class CacheResultHandler extends ResultHandler {

    @Override
    public Object handleResult(QueryContext context, CacheInfo info, CacheExecutor cacheExecutor, long startTime) {

        // 处理查询结果
        String execResult = info.getExecResult();
        log.info("{} act=handleResult msg=查询结果：key={} execResult={}", LOG_STR, context.getKey(), execResult);

        switch (execResult) {
            case SUCCESS -> {
                return cacheExecutor.hit(context, info);
            }
            case NEED_QUERY -> {
                return cacheExecutor.miss(context);
            }
            case SUCCESS_NEED_QUERY -> {
                return cacheExecutor.timeoutHit(context, info);
            }
            case NEED_WAIT -> {
                if (System.currentTimeMillis() - startTime > MAX_RETRY_TIME) {
                    log.warn("{} act=handleQueryResult msg=查询请求超过最大重试次数 key={}", LOG_STR, context.getKey());
                    return defaultResult(context);
                }

                ThreadUtil.sleep(RETRY_TIME_INTERVAL);
                CacheInfo waitInfo = cacheExecutor.wait(context);
                return handleResult(context, waitInfo, cacheExecutor, startTime);
            }
            default -> {
                log.error("{} act=handleQueryResult msg=未知的查询结果 key={} execResult={}", LOG_STR, context.getKey(),
                    execResult);
                return defaultResult(context);
            }
        }
    }
}
