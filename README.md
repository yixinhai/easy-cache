# Easy-Cache

一个基于Java语言开发的轻量级缓存解决方案。使用了Spring Boot、Redis、Guava Cache等技术，提供了简单易用的注解式缓存操作。并内置了缓存穿透防护、缓存降级等特性，支持分布式环境下的缓存/数据库一致性保证。

## 特性

*   基于注解的缓存操作
*   支持分布式缓存（Redis）
*   支持本地二级缓存（L2 Cache）
*   内置缓存穿透防护
*   支持缓存降级
*   支持缓存更新
*   支持多集群配置
*   支持延迟删除
*   支持缓存一致性保证

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.xh.easy</groupId>
    <artifactId>easy-cache</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置 Redis

在 `application.yml` 中配置 Redis 集群信息：

```yaml
easy:
  cache:
    clusters:
      - id: default
        host: localhost
        port: 6379
        password: your_password
```

实现分布式缓存service，实现BaseRedisService

```java
/**
 * @author yixinhai
 */
@Service
public class RedisService implements BaseRedisService {
    @Override
    public String getClusterId() {
        return null;
    }

    @Override
    public Map<String, String> hgetall(String key) {
        return null;
    }

    @Override
    public String hsetall(String key, Map<String, String> stringStringMap) {
        return null;
    }

    @Override
    public void hsetallnx(String key, Map<String, String> value, long seconds) {

    }

    @Override
    public void hset(String key, String hashFieldLockInfo, String unLock) {

    }
}
```

### 3. 使用示例

#### 查询缓存

```java
@Service
public class UserService {
    
    @CacheAble(
        clusterId = "default",
        prefix = "user",
        keys = {"#id"},
        expireTime = 600,            	// 默认600
        timeUnit = TimeUnit.SECONDS, 	// 默认单位：秒
        l2Cache = true,				 	// 默认false
        l2CacheProportion = 5,		 	// 默认5
        preventCachePenetration = true	// 默认false
    )
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}
```

#### 更新缓存

```java
@Service
public class UserService {
    
    @CacheUpdate(
        clusterId = "default",
        prefix = "user",
        keys = {"#user.id"}
    )
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}
```

## 注解说明

### @CacheAble

用于查询缓存，主要属性：

*   `clusterId`: 缓存集群ID
*   `prefix`: 缓存key前缀
*   `keys`: 缓存key内容，支持SpEL表达式
*   `expireTime`: 缓存过期时间，默认600秒
*   `timeUnit`: 时间单位，默认秒
*   `l2Cache`: 是否开启二级缓存，默认false
*   `l2CacheProportion`: 二级缓存请求比例（1-10），默认5
*   `preventCachePenetration`: 是否开启缓存穿透防护，默认false

### @CacheUpdate

用于更新缓存，主要属性：

*   `clusterId`: 缓存集群ID
*   `prefix`: 缓存key前缀
*   `keys`: 缓存key内容，支持SpEL表达式

## 高级特性

### 1. 缓存穿透防护

当开启 `preventCachePenetration = true` 时，如果查询结果为空，会缓存一个特殊值，防止缓存穿透。

### 2. 二级缓存

当 Redis 不可用时，可以开启二级缓存（`l2Cache = true`）作为降级方案。通过 `l2CacheProportion` 控制请求比例，默认50%的请求会使用本地缓存。

### 3. 缓存一致性

通过延迟删除机制保证缓存一致性：

*   更新操作时，先将缓存标记为锁定状态
*   在延迟删除时间窗口内，仍然返回旧值
*   延迟删除时间窗口结束后，缓存自动失效

### 4. 多集群支持

支持配置多个 Redis 集群，通过 `clusterId` 指定使用哪个集群。

### 5. 缓存健康度管理机制

`FaultManager` 是缓存健康度管理的核心类，负责监控和管理缓存集群的健康状态。主要功能包括：

*   集群可用性管理
*   单个缓存键可用性管理
*   集群探活管理
*   故障统计和容忍度控制

#### 关键特性

1.  **多级故障检测**
    *   集群级别故障检测
    *   单个缓存键故障检测
    *   实时探活机制

2.  **故障容忍度控制**
    *   时间窗口统计
    *   可配置的故障阈值
    *   自动故障恢复

3.  **降级策略**
    *   集群不可用自动降级
    *   二级缓存支持
    *   平滑切换机制

#### 降级策略

当检测到集群不可用时，系统会：

1.  标记集群为不可用状态
2.  启动集群探活任务
3.  如果配置了二级缓存，自动降级到本地缓存
4.  持续监控集群状态，等待恢复

#### 优势

1.  **实时监控**
    *   持续监控缓存健康状态
    *   快速响应故障情况

2.  **自动恢复**
    *   自动探活机制
    *   平滑恢复过程

3.  **可配置性**
    *   可调整的故障容忍度
    *   灵活的时间窗口设置

4.  **多级保护**
    *   集群级别保护
    *   单个缓存键保护
    *   二级缓存降级

## 注意事项

1.  确保 Redis 集群配置正确
2.  合理设置缓存过期时间
3.  根据业务需求选择是否开启二级缓存
4.  注意缓存key的设计，避免冲突
5.  合理使用缓存穿透防护

## 最佳实践

1.  对于读多写少的场景，建议开启二级缓存
2.  对于可能发生缓存穿透的接口，建议开启缓存穿透防护
3.  缓存key设计建议：`业务:模块:ID`
4.  合理设置过期时间，避免缓存雪崩
5.  更新操作时，确保 `@CacheUpdate` 的key与 `@CacheAble` 的key一致
