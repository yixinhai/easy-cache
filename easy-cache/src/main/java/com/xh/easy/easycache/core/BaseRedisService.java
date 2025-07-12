package com.xh.easy.easycache.core;

import java.util.Map;

public interface BaseRedisService {

    String getClusterId();

    Map<String, String> hgetall(String key);

    String hsetall(String key, Map<String, String> stringStringMap);

    void hsetallnx(String key, Map<String, String> value, long seconds);

    void hset(String key, String hashFieldLockInfo, String unLock);
}
