package com.xh.easy.easycache.base;

import com.xh.easy.easycache.exception.TargetMethodExecFailedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

public class JoinPointContext {

    private final ClassHandler classHandler;
    private final ProceedingJoinPoint pjp;
    private final Object bean;
    private final Class<?> beanClass;
    private final MethodSignature methodSignature;
    private final Method method;
    private final Object[] args;
    private final Class<?> resultType;

    public JoinPointContext(ProceedingJoinPoint pjp) {
        this.classHandler = ClassHandler.getInstance();
        this.pjp = pjp;
        this.bean = pjp.getTarget();
        this.beanClass = bean.getClass();
        this.methodSignature = (MethodSignature) pjp.getSignature();
        this.method = methodSignature.getMethod();
        this.args = pjp.getArgs();
        this.resultType = methodSignature.getReturnType();
    }

    public ProceedingJoinPoint getPjp() {
        return pjp;
    }

    public Object getBean() {
        return bean;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return "JoinPointContext{" +
                "pjp=" + pjp +
                ", bean=" + bean +
                ", methodSignature=" + methodSignature +
                ", method=" + method +
                ", args=" + Arrays.toString(args) +
                ", resultType=" + resultType +
                '}';
    }

    public Object proceed() throws TargetMethodExecFailedException {
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw new TargetMethodExecFailedException("msg=目标方法执行异常", e);
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Method declaredMethod = this.classHandler.getMethod(this.getBeanClass(), this.methodSignature.getName(),
                this.methodSignature.getParameterTypes());
        return this.classHandler.getDeclaredAnnotation(declaredMethod, annotationClass);
    }
}
