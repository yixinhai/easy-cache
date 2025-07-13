package com.xh.easy.easycache.utils.serialze;

import com.google.common.io.BaseEncoding;

/**
 * 抽象序列化器
 *
 * @author yixinhai
 */
public abstract class AbstractSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) throws SerializeException {
        return serialize(object, null);
    }

    @Override
    public String serialize2String(Object object) throws SerializeException {
        return serialize2String(object, null);
    }

    @Override
    public String serialize2String(Object object, ClassLoader classLoader) throws SerializeException {
        return BaseEncoding.base64().encode(serialize(object, classLoader));
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializeException {
        return data == null || data.length == 0 ? null : deserialize(data, clazz, null);
    }

    @Override
    public <T> T deserialize(String data, Class<T> clazz) throws SerializeException {
        return deserialize(data, clazz, null);
    }

    @Override
    public <T> T deserialize(String data, Class<T> clazz, ClassLoader classLoader) throws SerializeException {
        return data == null || data.length() == 0 ? null
            : deserialize(BaseEncoding.base64().decode(data), clazz, classLoader);
    }
}
