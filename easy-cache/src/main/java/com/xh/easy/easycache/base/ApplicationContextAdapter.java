package com.xh.easy.easycache.base;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;

public class ApplicationContextAdapter implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static Class<?> getClassByName(String name) {
        return context.getType(name);
    }

    public static <T> T getBeanByName(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    public static <T> T getBeanByType(Class<T> clazz) {
    	return context.getBean(clazz);
    }
}
