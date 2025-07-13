package com.xh.easy.easycache;

import com.xh.easy.easycache.core.BaseRedisService;

import java.util.Map;

/**
 * @author yixinhai
 */
public class RedisService implements BaseRedisService {
    @Override
    public String getClusterId() {
        return "cluster1";
    }

    @Override
    public Map<String, String> hgetall(String key) {
        return null;
    }

    @Override
    public String hsetall(String key, Map<String, String> stringStringMap) {
        return null;
    }

    @Override
    public void hsetallnx(String key, Map<String, String> value, long seconds) {

    }

    @Override
    public void hset(String key, String hashFieldLockInfo, String unLock) {

    }

    @Override
    public boolean ping() {
        return false;
    }
}
