package com.xh.easy.easycache.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassHandler {

    private static final ClassHandler CLASS_HANDLER = new ClassHandler();

    public static ClassHandler getInstance() {
        return CLASS_HANDLER;
    }

    public Method[] getMethods(Class<?> clazz) {
        return clazz.getMethods();
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public Object processMethod(Method method, Object bean, Object... args)
        throws InvocationTargetException, IllegalAccessException {

        if (null == method) {
            throw new IllegalArgumentException("method cannot be null");
        }

        return method.invoke(bean, args);
    }

    public Field getDeclaredField(Class<?> clazz, String fieldName) {
        if (null == clazz || null == fieldName || fieldName.length() == 0) {
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public Class<?> getDeclaredFieldType(Class<?> clazz, String fieldName) {
        Field field = getDeclaredField(clazz, fieldName);
        return null == field ? null : field.getType();
    }

    public static List<Field> getDeclaredFields(Class<?> clazz) {
        try {
            return Arrays.asList(clazz.getDeclaredFields());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Object getFieldContent(Field field, Object o) {
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public void setFieldContent(Field field, Object o, Object hidFieldContent) {
        try {
            field.setAccessible(true);
            field.set(o, hidFieldContent);
        } catch (IllegalAccessException ignore) {
        }
    }

    public <T extends Annotation> T getDeclaredAnnotation(Field field, Class<T> annotationClass) {
        return field.getDeclaredAnnotation(annotationClass);
    }

    public <T extends Annotation> T getDeclaredAnnotation(Method method, Class<T> annotationClass) {
        return method.getDeclaredAnnotation(annotationClass);
    }

    public boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return clazz.isAnnotationPresent(annotationClass);
    }

    public boolean isAnnotationPresent(Field field, Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    public boolean isPrimitiveType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.equals(String.class) || clazz.equals(Integer.class)
                || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class)
                || clazz.equals(Short.class) || clazz.equals(Byte.class) || clazz.equals(Character.class)
                || clazz.equals(Boolean.class);
    }

    /**
     * 获取调用方的详细信息
     * @return 包含类名、方法名、文件名和行号的字符串
     */
    public static String getCallerDetail() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return String.format("%s.%s(%s:%d)",
                    caller.getClassName(),
                    caller.getMethodName(),
                    caller.getFileName(),
                    caller.getLineNumber());
        }
        return "Unknown";
    }

    public static String getClassName(Object o) {
        return o.getClass().getName();
    }
}
