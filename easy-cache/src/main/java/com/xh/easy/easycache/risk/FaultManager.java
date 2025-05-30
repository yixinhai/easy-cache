package com.xh.easy.easycache.risk;

import com.xh.easy.easycache.ClusterConfiguration;
import com.xh.easy.easycache.base.TimeWindowCounter;
import com.xh.easy.easycache.handler.CacheExecutor;
import com.xh.easy.easycache.handler.RedisCache;
import com.xh.easy.easycache.risk.event.ClusterFault;
import com.xh.easy.easycache.risk.event.UpdateFailed;
import com.xh.easy.easycache.risk.event.UpdateSuccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 故障管理类
 *
 * @author yixinhai
 */
@Component
public class FaultManager extends TimeWindowCounter implements CacheVisitor {

    private static final long FAULT_STATISTICS_MS = 60000L;
    private static final int FAULT_TOLERABLE_QUANTITY = 100;
    private static final long CLUSTER_HEALTH_LEVEL_CHECK_MS = 1000L;
    private static final long CLUSTER_HEALTH_LEVEL_EXPLORE_MS = 100L;

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

    /**
     * 集群探活
     * key: 集群ID
     * value: 是否有探活任务正在进行
     */
    private final ConcurrentHashMap<String, AtomicBoolean> clusterExploreAble = new ConcurrentHashMap<>();

    /**
     * 集群可用性检查任务
     */
    private final ScheduledExecutorService healthyChecker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private ClusterConfiguration clusterConfiguration;

    @Autowired
    private CacheExecutor cacheExecutor;

    @Autowired
    private RedisCache redisCache;


    public FaultManager() {
        super(FAULT_STATISTICS_MS, FAULT_TOLERABLE_QUANTITY);
    }

    @PostConstruct
    public void init() {
        // 初始化集群可用性
        clusterConfiguration.getClusterIds()
                .forEach(clusterId -> clusterAvailable.put(clusterId, new AtomicBoolean(true)));

        // 初始化集群探活
        clusterConfiguration.getClusterIds()
                .forEach(clusterId -> clusterExploreAble.put(clusterId, new AtomicBoolean(true)));

        // 启动集群健康度检查任务
        healthyChecker.scheduleAtFixedRate(this::checkClusterHealthLevel, CLUSTER_HEALTH_LEVEL_CHECK_MS * 600,
                CLUSTER_HEALTH_LEVEL_CHECK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 集群健康度检查
     */
    private void checkClusterHealthLevel() {
        for (String clusterId : clusterConfiguration.getClusterIds()) {
            if (isClusterAvailable(null, clusterId) && getCount(clusterId) >= FAULT_TOLERABLE_QUANTITY) {

                // 集群不可用
                this.clusterNotAvailable(clusterId);

                // 集群探活
                this.exploreClusterHealthLevel(clusterId);
            }
        }
    }

    /**
     * 集群探活
     */
    private void exploreClusterHealthLevel(String clusterId) {
        if (clusterExploreAble.get(clusterId).compareAndSet(true, false)) {
            healthyChecker.scheduleAtFixedRate(() -> {

                // 探活操作
                if (cacheExecutor.ping(redisCache)) {
                    this.clusterAvailable(clusterId);
                }

            }, 0, CLUSTER_HEALTH_LEVEL_EXPLORE_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 标识集群缓存可用
     */
    private void clusterAvailable(String clusterId) {
        clusterAvailable.get(clusterId).set(true);
    }

    /**
     * 标识集群缓存不可用
     */
    private void clusterNotAvailable(String clusterId) {
        clusterAvailable.get(clusterId).set(false);
    }

    /**
     * 集群缓存是否可用
     */
    public boolean isClusterAvailable(String key, String clusterId) {
        return clusterAvailable.get(clusterId).get() && keyAvailable.getOrDefault(key, true);
    }


    @Override
    public void visit(UpdateFailed element) {
        keyAvailable.put(element.getKey(), false);

        // TODO 告警

        // TODO 数据上报
    }

    @Override
    public void visit(UpdateSuccess element) {
        keyAvailable.remove(element.getKey());

        // TODO 数据上报
    }

    @Override
    public void visit(ClusterFault element) {
        this.increment(element.getClusterId());
    }
}
