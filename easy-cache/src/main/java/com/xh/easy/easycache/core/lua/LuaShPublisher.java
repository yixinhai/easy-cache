package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.config.ClusterConfiguration;
import com.xh.easy.easycache.core.lua.event.NoScriptEvent;
import com.xh.easy.easycache.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.xh.easy.easycache.entity.constant.LogStrConstant.LOG_STR;

/**
 * 发布Lua脚本到Redis集群
 *
 * @author yixinhai
 */
@Slf4j
@Component
public class LuaShPublisher implements LoadScriptVisitor {

    @Autowired
    private RedisCommandsManager redisCommandsManager;

    @Autowired
    private ClusterConfiguration clusterConfiguration;


    @PostConstruct
    private void init() {

        // 单启一个线程，不影响主线程启动服务或占用线程池核心线程
        new Thread(() -> {
            for (;;) {
                try {
                    publish();
                    break;
                } catch (Exception e) {
                    log.warn("{} act=LuaShPublisher_init msg=发布Lua脚本异常", LOG_STR, e);
                }
            }
        });
    }

    /**
     * 发布Lua脚本到Redis服务器
     */
    protected void publish() {
        clusterConfiguration.getClusterIds()
            .forEach(clusterId -> doPublish(redisCommandsManager.getRedisCommands(clusterId)));
    }

    /**
     * 发布Lua脚本到Redis服务器
     *
     * @param connection Redis集群连接
     */
    private void doPublish(RedisConnection connection) {
        for (LuaSh.LuaShEnum sh : LuaSh.LuaShEnum.values()) {

            // 上传lua脚本到redis服务器
            String script = connection.scriptLoad(sh.getOperation());
            Assert.notBlank(script, "未获取到集群响应的SHA1 digest");

            // 记录脚本对应hash
            LuaSh.recordShDigest(sh.getOperation(), script);
        }
    }

    @Override
    public void visit(NoScriptEvent event) {
        try {
            doPublish(event.getConnection());
        } catch (Exception e) {
            log.warn("{} act=LuaShPublisher.visit msg=加载lua脚本到Redis集群异常", LOG_STR, e);
        }
    }
}
