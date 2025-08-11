# Easy-Cache

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📖 项目简介

Easy-Cache 是一个基于 Spring Boot 的高性能多级缓存框架，提供简单易用的注解式缓存操作。该框架支持 Redis 集群缓存和本地二级缓存，具备多级缓存动态升降级、容错机制、缓存穿透防护、弹性过期、最终一致性保障等高级特性。开发人员在开发需求时不需要额外编写代码保证一致性、穿透、雪崩、宕机问题，只需要在注解设置对应策略即可。
微信公众号：https://mp.weixin.qq.com/s/OENxcvRSGtkPtNzjWwK6KQ

【流程设计思路参照了RocksCache <https://github.com/dtm-labs/rockscache.git>】

## ✨ 核心特性

### 🚀 高性能多级缓存

*   **Redis 集群缓存**: 支持多 Redis 集群配置，提供分布式缓存能力
*   **本地二级缓存**: 基于 Guava Cache 的本地缓存，Redis 宕机时自动降级
*   **智能缓存策略**: 支持按比例缓存到本地，避免缓存雪崩

### 🛡️ 容错与高可用

*   **故障自动降级**: Redis 集群故障时自动切换到本地缓存，集群恢复后自动升级
*   **健康检查机制**: 实时监控缓存集群状态，动态调整缓存策略
*   **异步更新机制**: 支持异步缓存更新，提升系统响应速度

### 🔒 缓存安全

*   **防缓存穿透**: 内置空值缓存机制，防止恶意请求穿透到数据库
*   **分布式锁**: 基于 Redis 的分布式锁机制，防止缓存击穿
*   **弹性过期**: 支持缓存标记删除，解决缓存与数据库不一致问题

### 🎯 简单易用

*   **注解驱动**: 通过 `@CacheAble` 和 `@CacheUpdate` 注解即可实现缓存操作
*   **自动序列化**: 支持 JSON 序列化，无需手动处理对象转换
*   **灵活配置**: 支持多种缓存参数配置，满足不同业务场景需求

## 🏗️ 项目结构

    easy-cache/
    ├── aop/                    # 注解和切面处理
    │   ├── CacheAble.java     # 缓存查询注解
    │   ├── CacheUpdate.java   # 缓存更新注解
    │   └── CacheAspect.java   # 缓存切面
    ├── core/                   # 核心功能模块
    │   ├── dispatcher/        # 缓存调度器
    │   ├── executor/          # 缓存执行器
    │   ├── lua/              # Redis Lua脚本
    │   └── monitor/          # 监控模块
    ├── config/                # 配置类
    ├── entity/                # 实体类
    ├── exception/             # 异常类
    └── utils/                 # 工具类

## UML核心类图
<img width="4174" height="3212" alt="image" src="https://github.com/user-attachments/assets/d83a7e35-6e69-4583-a5ef-3d5a15fa8add" />

## 缓存一致性保证机制

Easy-Cache 通过分布式锁机制和Lua脚本原子操作，确保缓存数据的最终一致性。

#### 缓存数据结构

缓存中的数据是具有以下字段的哈希结构：

*   **value**: 数据本身
*   **lockInfo**: 锁定状态信息（'locked' 或 'unLock'）
*   **unlockTime**: 数据锁过期时间，当一个进程查询缓存没有数据时，则锁定缓存一小段时间，然后查询DB、更新缓存
*   **owner**: 数据锁UUID，标识当前锁的持有者

#### 查询缓存流程

查询缓存时，Lua脚本会执行以下逻辑：

1.  **如果数据为空且锁已过期**: 则锁定缓存，返回 `NEED_QUERY`，同步执行"取数据"并返回结果
2.  **如果数据为空且被锁定**: 则返回 `NEED_WAIT`，休眠100ms并再次查询
3.  **如果数据不为空且被锁定**: 则立即返回结果，异步执行"取数据"
4.  **如果数据不为空且未锁定**: 则立即返回结果

#### "取数据"操作定义

判断是否需要更新缓存，如果满足以下两个条件之一，则需要更新缓存：

*   数据为空且未锁定
*   数据锁定已过期

如果需要更新缓存，则锁定缓存，查询DB，如果验证锁持有者未更改，则更新并解锁缓存。

#### 缓存失效机制

当DB数据更新时，通过 `@CacheUpdate` 注解成功更新数据后，保证缓存被标记为已删除。

设置lockInfo锁定状态为 'locked'，并设置锁过期时间，下次查询到缓存时会触发"取数据"。

#### 最终一致性保证

通过上述策略，如果最后写入数据库的版本是Vi，最后写入缓存的版本是V，写入V的uuid是uuidv，那么一定有如下的事件序列：

    数据库写入Vi → 缓存数据标记为已删除 → 某些查询锁定数据并写入uuidv → 查询数据库结果V → 缓存中的owner是uuidv，写入结果V

在这个序列中，V的读取发生在Vi的写入之后，所以V等于Vi，保证了缓存数据的最终一致性。
<img width="3500" height="1538" alt="image" src="https://github.com/user-attachments/assets/55b95465-cc41-4d13-a7d8-f3309ad259f4" />


#### 核心实现

*   **CacheHashStructure**: 定义了缓存的哈希结构字段
*   **LuaExecResult**: 定义了Lua脚本的执行结果类型
*   **CacheDispatcher**: 实现了查询结果的处理逻辑
*   **MultiLevelCacheExecutor**: 实现了多级缓存的核心执行逻辑
*   **RedisCache**: 实现了Redis缓存的具体操作
*   **LuaSh**: 实现了保证原子性的Lua脚本

当开发者使用 `@CacheAble` 在读取数据时调用，并确保 `@CacheUpdate` 在更新数据库后调用，那么缓存就可以保证最终的一致性。

## 📦 技术栈

*   **Java 17+**: 使用最新的 Java 特性
*   **Spring Boot 2.7.5**: 基于 Spring Boot 框架
*   **Spring AOP**: 实现注解驱动的缓存切面
*   **Lettuce**: Redis 客户端，支持集群模式
*   **Guava Cache**: 本地缓存实现
*   **FastJSON**: JSON 序列化
*   **Lombok**: 简化代码编写
*   **Hutool**: 工具类库

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.xh.easy</groupId>
    <artifactId>easy-cache</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置 Redis 集群

```yaml
lettuce:
  redis:
    - name: cluster1
      addr: redis://localhost:6379
      password: your_password
      timeout: 1000
      connections:
        - name: redisConnection
```

### 3. 使用缓存注解

```java
@Service
public class UserService {
    
    @CacheAble(
        clusterId = "cluster1",
        prefix = "user",
        keys = {"#userId"},
        expireTime = 3600L,
        timeUnit = TimeUnit.SECONDS,
        l2Cache = true,
        l2CacheProportion = 5,
        preventCachePenetration = true
    )
    public User getUserById(Long userId) {
        // 从数据库查询用户信息
        return userRepository.findById(userId);
    }
    
    @CacheUpdate(
        clusterId = "cluster1",
        prefix = "user",
        keys = {"#user.id"}
    )
    public User updateUser(User user) {
        // 更新用户信息
        return userRepository.save(user);
    }
}
```

## 📋 注解说明

### @CacheAble 缓存查询注解

| 参数                        | 类型        | 默认值     | 说明                |
| ------------------------- | --------- | ------- | ----------------- |
| `clusterId`               | String    | -       | 缓存集群ID（必填）        |
| `prefix`                  | String    | -       | 缓存key前缀（必填）       |
| `keys`                    | String\[] | {}      | 缓存key内容，支持SpEL表达式 |
| `expireTime`              | long      | 600     | 缓存过期时间            |
| `timeUnit`                | TimeUnit  | SECONDS | 时间单位              |
| `l2Cache`                 | boolean   | false   | 是否开启二级缓存          |
| `l2CacheProportion`       | int       | 5       | 二级缓存请求比例（0-10）    |
| `preventCachePenetration` | boolean   | false   | 是否开启防缓存穿透         |
| `elasticExpirationTime`   | long      | 1500    | 弹性缓存过期时间（毫秒）      |

### @CacheUpdate 缓存更新注解

| 参数          | 类型        | 默认值 | 说明                |
| ----------- | --------- | --- | ----------------- |
| `clusterId` | String    | -   | 缓存集群ID（必填）        |
| `prefix`    | String    | -   | 缓存key前缀（必填）       |
| `keys`      | String\[] | {}  | 缓存key内容，支持SpEL表达式 |

## ⚙️ 配置参数

### 缓存配置常量

```java
// 默认缓存过期时间（秒）
DEFAULT_EXPIRE_TIME = 600L

// 弹性缓存标记删除时间（毫秒）
DEFAULT_ELASTIC_CACHE_EXPIRATION_TIME = 1500L

// 缓存被锁定时最大重试时间（毫秒）
MAX_RETRY_TIME = 1500L

// 缓存被锁定时重试时间间隔（毫秒）
RETRY_TIME_INTERVAL = 100L

// 获取缓存值时锁超时时间（毫秒）
GET_VALUE_UNLOCK_TIME = 1000L
```

## 🔧 高级特性

### 1. 多级缓存策略

```java
@CacheAble(
    clusterId = "cluster1",
    prefix = "product",
    keys = {"#productId"},
    l2Cache = true,
    l2CacheProportion = 7  // 70%的请求会缓存到本地
)
public Product getProduct(Long productId) {
    return productRepository.findById(productId);
}
```

### 2. 防缓存穿透

```java
@CacheAble(
    clusterId = "cluster1",
    prefix = "user",
    keys = {"#userId"},
    preventCachePenetration = true  // 开启防缓存穿透
)
public User getUserById(Long userId) {
    User user = userRepository.findById(userId);
    // 即使返回null也会被缓存，防止恶意请求
    return user;
}
```

### 3. 弹性过期机制

```java
@CacheAble(
    clusterId = "cluster1",
    prefix = "order",
    keys = {"#orderId"},
    elasticExpirationTime = 2000L  // 2秒弹性过期时间
)
public Order getOrder(Long orderId) {
    return orderRepository.findById(orderId);
}
```

### 4. Lua脚本原子性操作

为了保证缓存操作的原子性，Easy-Cache 使用 Lua 脚本执行所有 Redis 操作。同时，为了减少每次 Lua 脚本交互带来的额外网络 I/O，框架采用 `EVALSHA` 方式：

*   **服务启动时预加载**: 在应用启动时，将所有 Lua 脚本上传到 Redis 集群
*   **脚本哈希缓存**: 通过 Redis 为脚本分配的 SHA1 哈希值进行交互
*   **网络 I/O 优化**: 避免每次执行时传输完整的脚本内容，显著减少网络开销

```java
// Lua脚本示例：获取并锁定缓存值
private static final String GET_SH =
    "local key = KEYS[1]\n"
    + "local newUnlockTime = ARGV[1]\n"
    + "local owner = ARGV[2]\n"
    + "local currentTime = tonumber(ARGV[3])\n"
    + "local value = redis.call('HGET', key, 'value')\n"
    + "local unlockTime = redis.call('HGET', key, 'unlockTime')\n"
    + "local lockOwner = redis.call('HGET', key, 'owner')\n"
    + "local lockInfo = redis.call('HGET', key, 'lockInfo')\n"
    + "if unlockTime and currentTime > tonumber(unlockTime) then\n"
    + "    redis.call('HMSET', key, 'lockInfo', 'locked', 'unlockTime', 'newUnlockTime', 'owner', owner)\n"
    + "    return {value, 'NEED_QUERY'}\n"
    + "end\n"
    + "if not value or value == '' then\n"
    + "    if owner then\n"
    + "        return {value, 'NEED_WAIT'}\n"
    + "    end\n"
    + "    redis.call('HMSET', key, 'lockInfo', 'locked', 'unlockTime', newUnlockTime, 'owner', owner)\n"
    + "    return {value, 'NEED_QUERY'}\n"
    + "end\n"
    + "if lockInfo and lockInfo == 'locked' then \n"
    + "    return {value, 'SUCCESS_NEED_QUERY'}\n"
    + "end\n"
    + "return {value , 'SUCCESS'}";
```

### 5. 智能集群健康度监控

Easy-Cache 实现了基于 LRU 时间窗口的集群健康度监控系统，能够动态调整缓存策略：

*   **时间窗口统计**: 使用滑动时间窗口（默认60秒）统计 Redis 集群操作成功/失败事件
*   **故障容忍度控制**: 当故障事件达到阈值（默认100次）时，自动标记集群为不可用状态
*   **动态升降级**: 根据集群健康度自动切换缓存策略，实现智能降级和恢复
*   **实时探活机制**: 集群故障时启动探活任务，定期检测集群恢复情况

这种设计确保了：

*   **高可用性**: 集群故障时自动降级到本地缓存
*   **快速恢复**: 集群恢复后自动升级回分布式缓存
*   **性能优化**: 减少不必要的网络请求和资源消耗
*   **智能决策**: 基于实时数据做出最优的缓存策略选择

***

如果这个项目对您有帮助，请给个 ⭐️ 支持一下！
