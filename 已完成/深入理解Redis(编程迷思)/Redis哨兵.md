# Redis哨兵

## 1.哨兵概述

### 功能

- 监控(Monitoring)

	- 实时检查主节点和从节点是否运作正常

- 自动故障转移(Automatic failover)(核心)

	- 将故障主节点的从节点升级成新的主节点
	- 让其他从节点复制新的主节点

- 配置提供者(Configuration provider)

	- 为初始化客户端提供Redis服务的主节点地址

- 通知(Notification)

	- 将故障转移的结果发送给客户端

### 架构

- 哨兵节点

	- 由一个或多个特殊Redis节点组成，不存储数据
	- 监控数据节点

- 数据节点

	- 主节点和从节点
	- 从节点复制主节点

## 2.哨兵部署

### 部署主从节点

- 主节点配置

	- port 6379

- 从节点配置

	- port 6380
slaveof 192.168.92.128 6379
	- port 6381
slaveof 192.168.92.128 6379

- 启动主从节点

	- redis-server redis-6379.conf
	- redis-server redis-6380.conf
	- redis-server redis-6381.conf

- 查看主从状态

	- info Replication

### 部署哨兵节点

- 配置哨兵节点

	- port 26379
sentinel monitor mymaster 192.168.92.128 6379 2
	- port 26380
sentinel monitor mymaster 192.168.92.128 6379 2
	- port 26381
sentinel monitor mymaster 192.168.92.128 6379 2
	- mymaster，主节点名称
	- 2，至少2个哨兵节点判断主节点故障才进行转移

- 启动哨兵节点

	- redis-sentinel sentinel-26379.conf
	- redis-sentinel sentinel-26380.conf
	- redis-sentinel sentinel-26381.conf

- 查看哨兵状态

	- info Sentinel

- 查看哨兵节点新增配置

	- known-slave，发现的从节点
	- known-sentinel，发现的哨兵节点
	- -epoch，配置纪元

### 监控+故障转移

- 杀掉主节点(6379)
- 从节点(6380)切换成主节点

	- 配置重写(config write)

		- 新主节点(6380)去除slaveof配置
		- 从节点(6381)配置slaveof新主节点(6380)
		- 哨兵节点监视新主节点(6380)，纪元参数+1

- 重启节点(6379)为从节点

## 3.客户端访问哨兵

### 配置提供者

- 客户端通过哨兵节点+masterName获取主节点信息
- 客户端访问流程

	- 添加哨兵节点地址，Sentinels.add(ip:port)
	- 添加主节点名称，masterName = "mymaster"
	- 连接哨兵节点，new JedisSentinelPool(masterName, sentinels)
	- 客户端获取主节点连接，JedisSentinelPool .getResource()
	- 客户端发送命令到主节点，jedis.set(key,value)

- 原理

	- 客户端遍历连接哨兵节点
	- 调用哨兵节点的sentinel get-master-addr-by-name命令，获取主节点信息

### 通知

- 故障转移后，客户端会收到哨兵的通知，切换新主节点
- 原理

	- Redis的发布订阅功能
	- 客户端订阅哨兵节点的switch-master频道，收到消息则重新初始化连接池

## 4.哨兵实现原理

### 哨兵间的通信

- 哨兵实现功能依赖于节点通信(命令实现)
- 哨兵节点支持的命令

	- 基础查询

		- info sentinel，获取所有监控主节点基本信息
		- sentinel masters，获取所有监控主节点详细信息
		- sentinel master/slaves/sentinels mymaster，获取主节点mymaster的主节点/从节点/哨兵节点的详细信息
		- sentinel get-master-addr-by-name mymaster，获取主节点mymaster的地址信息
		- sentinel is-master-down-by-addr，哨兵间询问主节点是否下线，用于判断客观下线

	- 增加/移除监控

		- sentinel monitor mymaster2 192.168.92.128 16379 2，增加监控节点
		- sentinel remove mymaster2，移除监控节点

	- 强制故障转移

		- sentinel failover mymaster

### 哨兵功能基本原理

- 定时任务

	- 获取最新主从信息，向主节点发送info命令
	- 获取其他哨兵信息，发布订阅其他哨兵
	- 心跳检测下线，PING其他节点进行

- 主观下线

	- 心跳检测其他节点失败，则主观判断节点下线

- 客观下线

	- 哨兵判断主节点主观下线
	- sentinel is-master-down-by-addr命令询问其他哨兵该主节点是否下线
	- 判断主节点主观下线的哨兵数量达到一定数量，则对主节点进行客观下线

- 选举领导者哨兵

	- 哨兵判断主节点客观下线
	- Raft算法选举领导者哨兵对主节点进行故障转移

- 主节点故障转移

	- 在从节点中选择新的主节点

		- 选择依据，健康>优先级>offset>runid

	- 更新主从状态

		- slaveof no one新主节点
		- slaveof其他从节点(包括旧主节点)

## 5.哨兵配置

### 核心配置

- sentinel monitor {masterName} {masterIp} {masterPort} {quorum}，增加监控节点

	- {quorum}是判断主节点客观下线所需哨兵数量，建议为哨兵数量/2+1

- sentinel down-after-milliseconds {masterName} {time}，哨兵心跳检测PING命令超时时间

	- 默认30000，即30s
	- 值大，主观下线误判少
	- 值小，故障发现和转移时间短，可用性高

- sentinel parallel-syncs {masterName} {number}，故障转移从节点并发复制个数

	- 默认1
	- 值大，从节点复制快
	- 值小，主节点压力小

- sentinel failover-timeout {masterName} {time}，故障转移子阶段超时时间

	- 默认180000，即180s
	- 超时则导致失败，重新故障转移时间为2倍

### 实践建议

- 哨兵节点冗余

	- 避免哨兵节点故障降低系统可用性
	- 减少客观下线的误判
	- 部署在不同物理机

- 哨兵节点奇数

	- 易判断主观下线
	- 易选举领导者

- 哨兵节点配置一致
- 哨兵节点容器部署

	- 端口映射可能导致哨兵节点通信失败

- 哨兵节点无法对从节点进行故障转移

*XMind - Trial Version*