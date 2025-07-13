package com.xh.easy.easycache.core.monitor.healthy;

import com.xh.easy.easycache.config.ClusterConfiguration;
import com.xh.easy.easycache.base.ClassHandler;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 故障信息类
 *
 * @author yixinhai
 */
public class ClusterHealthInfo {

    private static final ClusterHealthInfo INSTANCE = new ClusterHealthInfo();

    private final ClusterConfiguration clusterConfiguration;

    /**
     * 集群可用性
     * key: 集群ID
     * value: 是否可用
     */
    private static final ConcurrentHashMap<String, AtomicBoolean> clusterAvailable = new ConcurrentHashMap<>();

    /**
     * 缓存单个key可用性
     * key: 缓存key
     * value: 是否可用
     */
    private static final ConcurrentHashMap<String, Boolean> keyAvailable = new ConcurrentHashMap<>();


    private ClusterHealthInfo() {
        this.clusterConfiguration = ClassHandler.getBeanByType(ClusterConfiguration.class);
    }

    public static ClusterHealthInfo getInstance() {
        return INSTANCE;
    }

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
    public static boolean isClusterAvailable(String key, String clusterId) {
        return clusterAvailable.get(clusterId).get() && keyAvailable.getOrDefault(key, true);
    }
}
