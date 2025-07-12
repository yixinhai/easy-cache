package com.xh.easy.easycache.risk.healthy;

import com.xh.easy.easycache.ClusterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 故障管理类
 *
 * @author yixinhai
 */
@Component
public class ClusterHealthInfo {

    /**
     * 集群可用性
     * key: 集群ID
     * value: 是否可用
     */
    private final ConcurrentHashMap<String, AtomicBoolean> clusterAvailable = new ConcurrentHashMap<>();

    /**
     * 缓存单个key可用性
     * key: 缓存key
     * value: 是否可用
     */
    private final ConcurrentHashMap<String, Boolean> keyAvailable = new ConcurrentHashMap<>();

    @Autowired
    private ClusterConfiguration clusterConfiguration;

    @PostConstruct
    public void init() {
        // 初始化集群可用性
        clusterConfiguration.getClusterIds()
                .forEach(clusterId -> clusterAvailable.put(clusterId, new AtomicBoolean(true)));
    }

    /**
     * 标识集群缓存可用
     */
    protected void clusterAvailable(String clusterId) {
        clusterAvailable.get(clusterId).set(true);
    }

    /**
     * 标识集群缓存不可用
     */
    protected void clusterNotAvailable(String clusterId) {
        clusterAvailable.get(clusterId).set(false);
    }

    /**
     * 标识缓存key不可用
     *
     * @param key 缓存key
     */
    protected void keyNotAvailable(String key) {
        keyAvailable.put(key, false);
    }

    /**
     * 标识缓存key可用
     *
     * @param key 缓存key
     */
    protected void keyAvailable(String key) {
        keyAvailable.remove(key);
    }

    /**
     * 集群缓存是否可用
     */
    public boolean isClusterAvailable(String key, String clusterId) {
        return clusterAvailable.get(clusterId).get() && keyAvailable.getOrDefault(key, true);
    }
}
