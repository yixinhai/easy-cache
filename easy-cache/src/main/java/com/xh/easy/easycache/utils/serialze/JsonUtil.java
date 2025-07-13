package com.xh.easy.easycache.utils.serialze;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * json序列化工具类
 *
 * @author yixinhai
 */
public class JsonUtil {

    private static final SerializerFeature[] features = new SerializerFeature[]{
        SerializerFeature.IgnoreErrorGetter,
        SerializerFeature.IgnoreNonFieldGetter,
        SerializerFeature.WriteMapNullValue,
        SerializerFeature.SkipTransientField,
    };

    public static <T> T parseObject(byte[] data, Class<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

    public static JSONObject parseObject(String data) {
        return JSON.parseObject(data);
    }

    public static byte[] toJSONBytes(Object object) {
        return JSON.toJSONBytes(object, features);
    }
}
