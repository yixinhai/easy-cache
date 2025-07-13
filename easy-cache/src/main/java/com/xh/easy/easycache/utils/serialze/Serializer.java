package com.xh.easy.easycache.utils.serialze;

import java.lang.reflect.Type;

/**
 * 序列化器
 *
 * @author yixinhai
 */
public interface Serializer {

    /**
     * 获取序列化类型
     *
     * @return 序列化类型
     */
    SerializeType type();

    /**
     * 序列化
     *
     * @param object
     *     需要序列化的对象
     * @return 序列化后的byte数组
     * @throws SerializeException
     *     序列化异常
     */
    byte[] serialize(Object object) throws SerializeException;

    /**
     * 序列化
     *
     * @param object
     *     需要序列化的对象
     * @param classLoader
     *     类加载器
     * @return 序列化后的byte数组
     * @throws SerializeException
     *     序列化异常
     */
    byte[] serialize(Object object, ClassLoader classLoader) throws SerializeException;

    /**
     * 序列化
     *
     * @param object
     *     需要序列化的对象
     * @return 序列化后的字符串
     * @throws SerializeException
     *     序列化异常
     */
    String serialize2String(Object object) throws SerializeException;

    /**
     * 序列化
     *
     * @param object
     *     需要序列化的对象
     * @param classLoader
     *     类加载器
     * @return 序列化后的字符串
     * @throws SerializeException
     *     序列化异常
     */
    String serialize2String(Object object, ClassLoader classLoader) throws SerializeException;

    /**
     * 反序列化
     *
     * @param data
     *     需要反序列化的数据
     * @param clazz
     *     反序列化后的类型
     * @return 反序列化后的对象
     * @throws SerializeException
     *     反序列化异常
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws SerializeException;

    /**
     * 反序列化
     *
     * @param data
     *     需要反序列化的数据
     * @param clazz
     *     反序列化后的类型
     * @param classLoader
     *     类加载器
     * @return 反序列化后的对象
     * @throws SerializeException
     *     反序列化异常
     */
    <T> T deserialize(byte[] data, Class<T> clazz, ClassLoader classLoader) throws SerializeException;

    /**
     * 反序列化
     *
     * @param data
     *     需要反序列化的数据
     * @param clazz
     *     反序列化后的类型
     * @return 反序列化后的对象
     * @throws SerializeException
     *     反序列化异常
     */
    <T> T deserialize(String data, Class<T> clazz) throws SerializeException;

    /**
     * 反序列化
     *
     * @param data
     *     需要反序列化的数据
     * @param clazz
     *     反序列化后的类型
     * @param classLoader
     *     类加载器
     * @return 反序列化后的对象
     * @throws SerializeException
     *     反序列化异常
     */
    <T> T deserialize(String data, Class<T> clazz, ClassLoader classLoader) throws SerializeException;

    /**
     * 反序列化
     *
     * @param data
     *     需要反序列化的数据
     * @param type
     *     反序列化后的类型
     * @return 反序列化后的对象
     * @throws SerializeException
     *     反序列化异常
     */
    Object deserialize(String data, Type type) throws SerializeException;


    enum SerializeType {
        HESSIAN,
        JSON
    }
}
