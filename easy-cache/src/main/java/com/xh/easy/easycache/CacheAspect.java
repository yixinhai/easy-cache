package com.xh.easy.easycache;

import com.xh.easy.easycache.context.QueryContext;
import com.xh.easy.easycache.context.UpdateContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 缓存切面
 * @author yixinhai
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    @Autowired
    private CacheDispatcher dispatcher;

    @Around("@annotation(cacheAble)")
    public Object around(ProceedingJoinPoint pjp, CacheAble cacheAble) {
        return dispatcher.doDispatch(new QueryContext(pjp));
    }

    @Around("@annotation(cacheUpdate)")
    public Object around(ProceedingJoinPoint pjp, CacheUpdate cacheUpdate) {
        return dispatcher.doDispatch(new UpdateContext(pjp));
    }
}
