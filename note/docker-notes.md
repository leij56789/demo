# Docker 部署问题与解决方案总结

## 一、端口占用问题

**错误信息**：
```
ports are not available: exposing port TCP 0.0.0.0:3306
```

**原因**：本地 Windows MySQL 服务占用了 3306 端口

**解决方案**：
```powershell
# 方案一：停止本地 MySQL 服务
net stop MySQL80

# 方案二：让 Docker 使用其他端口
docker run -d --name mysql-demo -p 3307:3306 mysql:8.0
```

---

## 二、容器网络不通

**错误信息**：
```
Caused by: java.net.UnknownHostException: mysql-demo
```

**原因**：容器不在同一个 Docker 网络中

**解决方案**：
```powershell
# 1. 创建自定义网络
docker network create demo-net

# 2. 所有容器都使用该网络
docker run -d --name mysql-demo --network demo-net ...
docker run -d --name redis-demo --network demo-net ...
docker run -d --name rabbitmq-demo --network demo-net ...
docker run -d --name demo-app --network demo-net ...

# 3. 验证网络
docker network inspect demo-net
```

---

## 三、JDK 17 与 Docker 兼容性问题

**错误信息**：
```
Caused by: java.lang.NullPointerException: Cannot invoke "jdk.internal.platform.CgroupInfo.getMountPoint()"
```

**原因**：JDK 17 在旧版 Docker 环境中的 cgroup 检测问题

**解决方案**：
```powershell
# 添加 JVM 参数
docker run -d -e JAVA_TOOL_OPTIONS="-Djdk.lang.Process.launchMechanism=POSIX_SPAWN" ...
```

---

## 四、镜像拉取慢/失败

**错误信息**：长时间卡在 Pulling，或 `failed to fetch oauth token`

**原因**：访问 Docker Hub 网络慢

**解决方案**：
```json
// Docker Desktop → Settings → Docker Engine
{
  "registry-mirrors": [
    "https://docker.1panel.live",
    "https://dockerpull.org",
    "https://docker.m.daocloud.io"
  ]
}
```

---

## 五、RabbitMQ 反序列化错误

**错误信息**：
```
Attempt to deserialize unauthorized class java.util.HashMap
```

**原因**：Spring AMQP 安全限制

**解决方案**：
```powershell
# 添加环境变量
docker run -d -e SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true ...
```

---

## 六、RabbitMQ 无限错误打印

**错误信息**：
```
PRECONDITION_FAILED - unknown delivery tag 1
```

**原因**：确认方式不匹配（手动确认 vs 自动确认）

**解决方案**：
```java
// 消费者方法去掉 Channel 参数，使用自动确认
@RabbitListener(queues = "user.queue")
public void handleMessage(Map<String, Object> message) {
    // 处理消息，不需要手动 ack
}
```

---

## 七、Maven 命令找不到

**错误信息**：
```
mvn : 无法将"mvn"项识别为 cmdlet、函数...
```

**原因**：Maven 未配置环境变量

**解决方案**：
```powershell
# 1. 配置环境变量 Path，添加 Maven bin 目录
# 2. 重新打开 PowerShell
# 3. 或在 IDEA 中手动打包
```

---

## 八、应用启动时数据库未就绪

**错误信息**：
```
Communications link failure
```

**原因**：MySQL 启动慢，应用启动快

**解决方案**：
```powershell
# 脚本中添加等待逻辑
Start-Sleep -Seconds 10
# 或使用健康检查循环等待
```

---

## 九、完整正确的启动命令

```powershell
# 1. 创建网络
docker network create demo-net

# 2. 启动 MySQL
docker run -d --name mysql-demo --network demo-net `
    -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=test_db `
    -p 3306:3306 -v mysql-data:/var/lib/mysql mysql:8.0

# 3. 启动 Redis
docker run -d --name redis-demo --network demo-net -p 6379:6379 redis:alpine

# 4. 启动 RabbitMQ
docker run -d --name rabbitmq-demo --network demo-net `
    -p 5672:5672 -p 15672:15672 `
    -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest `
    rabbitmq:management

# 5. 启动应用
docker run -d --name demo-app --network demo-net `
    -e "SPRING_DATASOURCE_URL=jdbc:mysql://mysql-demo:3306/test_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai" `
    -e SPRING_DATASOURCE_USERNAME=root `
    -e SPRING_DATASOURCE_PASSWORD=123456 `
    -e SPRING_DATA_REDIS_HOST=redis-demo `
    -e SPRING_DATA_REDIS_PORT=6379 `
    -e SPRING_RABBITMQ_HOST=rabbitmq-demo `
    -e SPRING_RABBITMQ_PORT=5672 `
    -e SPRING_RABBITMQ_USERNAME=guest `
    -e SPRING_RABBITMQ_PASSWORD=guest `
    -e SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true `
    -e JAVA_TOOL_OPTIONS="-Djdk.lang.Process.launchMechanism=POSIX_SPAWN" `
    -p 8080:8080 demo:1.0
```

---

## 十、一键管理脚本

```powershell
# 启动所有容器
.\\\\docker-manager.ps1 start

# 停止所有容器
.\\\\docker-manager.ps1 stop

# 更新代码后重新部署
.\\\\docker-manager.ps1 rebuild

# 查看状态
.\\\\docker-manager.ps1 status

# 清理所有容器（保留 MySQL 数据）
.\\\\docker-manager.ps1 clean
```