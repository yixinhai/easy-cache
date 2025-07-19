package com.xh.easy.easycache.core.lua;

import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 缓存命令管理类
 *
 * @author yixinhai
 */
@Component
public class RedisCommandsManager {

    @Resource(name = "redisConnection")
    private StatefulRedisConnection<String, String> connection;

    public RedisCommandsAdapter getRedisCommands(String clusterId) {
        return EvalShaAdapter.of(connection.sync());
    }
}
