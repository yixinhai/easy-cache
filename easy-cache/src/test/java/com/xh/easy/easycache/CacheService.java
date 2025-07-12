package com.xh.easy.easycache;

import com.xh.easy.easycache.aop.CacheAble;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author yixinhai
 */
@Service
public class CacheService {

    @CacheAble(clusterId = "cluster1", prefix = "getInfo", keys = {"#infoId"}, timeUnit = TimeUnit.MILLISECONDS)
    public String getInfo(Long infoId) {
        return "info";
    }
}
