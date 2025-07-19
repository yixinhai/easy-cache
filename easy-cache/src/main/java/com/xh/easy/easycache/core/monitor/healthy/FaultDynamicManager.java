package com.xh.easy.easycache.core.monitor.healthy;

import com.xh.easy.easycache.base.ApplicationContextAdapter;
import com.xh.easy.easycache.config.ClusterConfiguration;
import com.xh.easy.easycache.base.TimeWindowCounter;
import com.xh.easy.easycache.core.executor.CacheHealthyChecker;
import com.xh.easy.easycache.core.executor.handler.RedisCache;
import com.xh.easy.easycache.core.monitor.healthy.event.ClusterFault;
import com.xh.easy.easycache.core.monitor.healthy.event.UpdateFailed;
import com.xh.easy.easycache.core.monitor.healthy.event.UpdateSuccess;
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
public class FaultDynamicManager implements CacheVisitor {

    /**
     * 故障统计时间窗口
     */
    private static final long FAULT_STATISTICS_MS = 60000L;

    /**
     * 故障事件队列大小
     */
    private static final int FAULT_TOLERABLE_QUANTITY = 100;

    /**
     * 集群健康度检查时间间隔
     */
    private static final long CLUSTER_HEALTH_LEVEL_CHECK_MS = 1000L;

    /**
     * 集群健康度探活时间间隔
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

    /**
     * 故障统计
     */
    private static final TimeWindowCounter faultCounter = new TimeWindowCounter(FAULT_STATISTICS_MS,
        FAULT_TOLERABLE_QUANTITY);

    private static final FaultDynamicManager INSTANCE = new FaultDynamicManager();

    private final ClusterHealthInfo clusterHealthInfo;

    private final ClusterConfiguration clusterConfiguration;

    private final CacheHealthyChecker redisCache;


    private FaultDynamicManager() {
        this.clusterHealthInfo = ClusterHealthInfo.getInstance();
        this.redisCache = RedisCache.getInstance();
        this.clusterConfiguration = ApplicationContextAdapter.getBeanByType(ClusterConfiguration.class);
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
        if (!element.sendByCluster()) {
            return;
        }

        clusterHealthInfo.keyNotAvailable(element.getKey());
    }

    @Override
    public void visit(UpdateSuccess element) {
        if (!element.sendByCluster()) {
            return;
        }

        clusterHealthInfo.keyAvailable(element.getKey());
    }

    @Override
    public void visit(ClusterFault element) {
        if (element.sendByCluster()) {
            return;
        }

        faultCounter.increment(element.getClusterId());
    }

    /**
     * 集群健康度检查
     */
    private void checkClusterHealthLevel() {
        for (String clusterId : clusterConfiguration.getClusterIds()) {
            if (!ClusterHealthInfo.isClusterAvailable(clusterId)
                && faultCounter.getCount(clusterId) >= FAULT_TOLERABLE_QUANTITY) {

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
