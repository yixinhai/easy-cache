package com.xh.easy.easycache;

import com.xh.easy.easycache.handler.BaseRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class ClusterConfiguration {

    private final Map<String, BaseRedisService> serviceMap = new HashMap<>();

    @Autowired
    private List<BaseRedisService> cacheService;

    @PostConstruct
    public void init() {
        cacheService.forEach(service -> {
            serviceMap.put(service.getClusterId(), service);
        });
    }

    public Set<String> getClusterIds() {
        return serviceMap.keySet();
    }

    public BaseRedisService getRedisService(String clusterId) {
        return serviceMap.get(clusterId);
    }
}
