package com.xh.easy.easycache.core.healthy;

import com.xh.easy.easycache.config.ClusterConfiguration;
import com.xh.easy.easycache.base.ClassHandler;
import com.xh.easy.easycache.base.TimeWindowCounter;
import com.xh.easy.easycache.core.executor.MultiLevelCacheExecutor;
import com.xh.easy.easycache.core.executor.RedisCache;
import com.xh.easy.easycache.core.healthy.event.ClusterFault;
import com.xh.easy.easycache.core.healthy.event.UpdateFailed;
import com.xh.easy.easycache.core.healthy.event.UpdateSuccess;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 故障动态管理
 *
 * @author yixinhai
 */
@Slf4j
public class FaultDynamicManager extends TimeWindowCounter implements CacheVisitor {

    /**
     *
     */
    private static final long FAULT_STATISTICS_MS = 60000L;

    /**
     * 故障事件队列大小
     */
    private static final int FAULT_TOLERABLE_QUANTITY = 100;

    /**
     *
     */
    private static final long CLUSTER_HEALTH_LEVEL_CHECK_MS = 1000L;

    /**
     *
     */
    private static final long CLUSTER_HEALTH_LEVEL_EXPLORE_MS = 100L;

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

    private static final FaultDynamicManager INSTANCE = new FaultDynamicManager();

    private final ClusterHealthInfo clusterHealthInfo;

    private final ClusterConfiguration clusterConfiguration;

    private final MultiLevelCacheExecutor redisCache;


    private FaultDynamicManager() {
        super(FAULT_STATISTICS_MS, FAULT_TOLERABLE_QUANTITY);
        this.clusterHealthInfo = ClusterHealthInfo.getInstance();
        this.redisCache = RedisCache.getInstance();
        this.clusterConfiguration = ClassHandler.getBeanByType(ClusterConfiguration.class);
    }

    public static FaultDynamicManager getInstance() {
        return INSTANCE;
    }

    @PostConstruct
    public void init() {

        // 初始化集群探活
        clusterConfiguration.getClusterIds()
            .forEach(clusterId -> clusterExploreAble.put(clusterId, new AtomicBoolean(true)));

        // 启动集群健康度检查任务
        healthyChecker.scheduleAtFixedRate(this::checkClusterHealthLevel, CLUSTER_HEALTH_LEVEL_CHECK_MS * 600,
            CLUSTER_HEALTH_LEVEL_CHECK_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void visit(UpdateFailed element) {
        clusterHealthInfo.keyNotAvailable(element.getKey());
    }

    @Override
    public void visit(UpdateSuccess element) {
        clusterHealthInfo.keyAvailable(element.getKey());
    }

    @Override
    public void visit(ClusterFault element) {
        this.increment(element.getClusterId());
    }

    /**
     * 集群健康度检查
     */
    private void checkClusterHealthLevel() {
        for (String clusterId : clusterConfiguration.getClusterIds()) {
            if (ClusterHealthInfo.isClusterAvailable(null, clusterId)
                && getCount(clusterId) >= FAULT_TOLERABLE_QUANTITY) {

                // 集群不可用
                clusterHealthInfo.clusterNotAvailable(clusterId);

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
                if (redisCache.ping(clusterId)) {
                    clusterHealthInfo.clusterAvailable(clusterId);
                }

            }, 0, CLUSTER_HEALTH_LEVEL_EXPLORE_MS, TimeUnit.MILLISECONDS);
        }
    }
}
