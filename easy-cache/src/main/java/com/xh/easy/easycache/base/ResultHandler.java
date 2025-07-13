package com.xh.easy.easycache.base;

import java.util.Objects;

/**
 * aop结果处理类
 *
 * @author yixinhai
 */
public class ResultHandler {

    public static <T extends JoinPointContext> Object defaultResult(T context) {
        Class<?> resultType = context.getResultClass();
        if (Objects.equals(resultType, Boolean.TYPE)) {
            return false;
        }
        return resultType.isPrimitive() ? 0 : null;
    }
}
