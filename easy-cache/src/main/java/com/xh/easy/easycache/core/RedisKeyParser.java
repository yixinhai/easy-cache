package com.xh.easy.easycache.core;

import com.xh.easy.easycache.base.SpelParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedisKeyParser extends SpelParser {

    private final String prefix;
    private String[] keys;

    public RedisKeyParser(String prefix, String[] definitionKeys, Method method, Object[] parameterValues) {
        super(definitionKeys, method, parameterValues);
        this.prefix = prefix;
        this.keys = definitionKeys;
    }

    public String parseKey() {
        if (null == prefix || 0 == prefix.length()) {
            return null;
        }
        String key = prefix;
        if (null != keys && keys.length > 0) {
            keys = parse();
            // key列表中不能有null值
            List<String> nullKey = Arrays.stream(keys)
                    .filter(k -> null != k && k.length() > 0)
                    .toList();
            if (0 == nullKey.size()) {
                return null;
            }
            key = key + ":" + String.join("_", keys);
        }
        return key;
    }
}
