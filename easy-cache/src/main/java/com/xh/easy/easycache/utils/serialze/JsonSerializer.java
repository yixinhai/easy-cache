package com.xh.easy.easycache.utils.serialze;

import cn.hutool.json.JSONUtil;

import java.lang.reflect.Type;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * json序列化器
 *
 * @author yixinhai
 */
public class JsonSerializer extends AbstractSerializer {

    @Override
    public SerializeType type() {
        return SerializeType.JSON;
    }

    @Override
    public byte[] serialize(Object object, ClassLoader classLoader) throws SerializeException {
        ClassLoader swap = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
            return JsonUtil.toJSONBytes(object);
        } catch (Throwable t) {
            throw new SerializeException(LOG_STR + " Failed to serialize json", t);
        } finally {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(swap);
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz, ClassLoader classLoader) throws SerializeException {
        ClassLoader swap = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
            return JsonUtil.parseObject(data, clazz);
        } catch (Throwable t) {
            throw new SerializeException(LOG_STR + " Failed to deserialize json", t);
        } finally {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(swap);
            }
        }
    }

    @Override
    public Object deserialize(String data, Type type) throws SerializeException {
        try {
            return JSONUtil.toBean(data, type, false);
        } catch (Exception e) {
            throw new SerializeException(LOG_STR + " Failed to deserialize json by type", e);
        }
    }
}
