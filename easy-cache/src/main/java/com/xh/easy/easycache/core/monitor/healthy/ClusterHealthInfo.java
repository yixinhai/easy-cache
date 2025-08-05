package com.xh.easy.easycache.core.monitor.healthy;

import com.xh.easy.easycache.base.ApplicationContextAdapter;
import com.xh.easy.easycache.config.ClusterConfiguration;

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


    private ClusterHealthInfo() {

        // 初始化集群可用性
        initClusterAvailable();
    }

    public static ClusterHealthInfo getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化集群可用性
     */
    private void initClusterAvailable() {
        ClusterConfiguration configuration = ApplicationContextAdapter.getBeanByType(ClusterConfiguration.class);
        configuration.getClusterIds()
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
     *
     * @param key       缓存key
     * @param clusterId 集群ID
     */
    public boolean isClusterAvailable(String key, String clusterId) {
        return isClusterAvailable(clusterId) && keyAvailable.getOrDefault(key, true);
    }

    /**
     * 集群缓存是否可用
     *
     * @param clusterId 集群ID
     */
    public boolean isClusterAvailable(String clusterId) {
        return clusterAvailable.get(clusterId).get();
    }
}
