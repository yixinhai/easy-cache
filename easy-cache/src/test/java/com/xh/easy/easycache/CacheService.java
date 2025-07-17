package com.xh.easy.easycache;

import com.xh.easy.easycache.aop.CacheAble;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author yixinhai
 */
@Service
public class CacheService {

    @CacheAble(clusterId = "cluster1",
        prefix = "getInfo",
        keys = {"#infoId"},
        expireTime = 5000L,
        timeUnit = TimeUnit.MILLISECONDS,
        l2Cache = true,
        l2CacheProportion = 4,
        preventCachePenetration = true,
        elasticExpirationTime = 1000L)
    public String getInfo(Long infoId) {
        return "info";
    }
}
