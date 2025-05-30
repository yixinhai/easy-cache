package com.xh.easy.easycache.base;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;

public class ApplicationContextHandler implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Class<?> getClassByName(String name) {
        return applicationContext.getType(name);
    }

    public Object getBeanByName(String name, Class<?> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public <T> T getBeanByType(Class<T> clazz) {
    	return applicationContext.getBean(clazz);
    }
}
