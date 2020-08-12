# Redis持久化

## 1.Redis高可用

### 概念

- 提供正常服务的时间(99.9%/99.99%/99.999%)
- 数据容量的扩展
- 数据安全不丢失

### 实现技术

- 持久化(数据备份)

	- RDB，将当前数据生成快照保存在硬盘
	- AOF，将每次执行的写命令保存到硬盘

- 复制

	- 数据的多机备份
	- 读操作的负载均衡
	- 简单的故障恢复

- 哨兵，自动化故障恢复
- 集群

	- 写操作的负载均衡
	- 存储能力突破单机限制

## 2.RDB持久化

### 手动触发

- save命令

	- 阻塞Redis直到RDB文件创建完毕

- bgsave命令

	- fork子进程用于创建RDB文件

### 自动触发

- 配置文件中配置save m n

	- m秒内，Redis数据发生至少n次变化，则执行bgsave
	- 依赖函数

		- serverCron函数

			- 每100ms执行维护服务器状态

		- dirty计数器

			- 记录上一次bgsave/save后服务器进行多少次修改，执行bgsave/save则清零

		- lastsave时间戳

			- 记录上一次成功执行bgsave/save的时间

	- 实现原理

		- serverCron检查save m n配置条件
		- 当前时间戳=lastsave > m
		- dirty >= n

- 主从复制

	- 主节点执行bgsave
	- 将RDB文件发送给从节点进行全量复制操作

- Shutdown命令关闭Redis，自动执行RDB持久化

### 执行流程

- 确保当前未执行save/bgsave/bgrewriteaof
- 父进程阻塞请求，fork子进程
- 父进程恢复响应，子进程根据父进程内存快照生成RDB文件
- 子进程发送完成信号给父进程，父进程更新统计信息

### RDB文件

- 存储路径

	- 启动前配置指定，默认Redis根目录/dump.rdb
	- 启动后命令设定

		- config set dir {newDir}
		- config set dbfilename {newRdbFileName}

- 文件格式

	- REDIS，常量字符
	- db_version，RDB文件版本号
	- SELECTDB dbNum pairs，完整的第dbNum号数据库的键值对信息
	- EOF，标志RDB文件正文结束
	- check_sum，内容校验和，判断文件是否损坏

- 启动时自动加载，但AOF的优先级更高
- 文件压缩

	- 默认采用LZF算法对数据进行压缩，可减小RDB文件体积

### 常用配置和默认值

- save m n
- stop-writes-on-bgsave-error yes

	- bgsave出现错误Redis是否停止执行写命令

- rdbcompression yes

	- 是否开启RDB文件压缩

- rdbchecksum yes

	- 是否开启RDB文件校验

- dbfilename dump.rdb

	- RDB文件名

- dir ./

	- RDB/AOF文件所在目录

## 3.AOF持久化

### 开启AOF

- 默认开启RDB，关闭AOF
- 可配置appendonly yes开启AOF

### 执行流程

- 命令追加(append)

	- Redis将写命令追加到AOF缓冲区(aof_write_buf)

- 文件写入(write)和文件同步(fsync)

	- 操作系统write函数，将AOF缓冲区数据暂存到内存缓冲区中
	- 操作系统fsync函数，将内存缓冲区数据写入硬盘文件中
	- 文件同步策略(appendfsync)

		- always，append+write+fsync
		- no，append+write，fsync由操作系统负责(30s)
		- everysec(默认)，append+write，fsync由专门线程每秒调用

- 文件重写(rewrite)

	- 将Redis进程内的数据转换成写命令同步到新AOF文件
	- 重写压缩原理

		- 过期数据不重写
		- 无效命令不重写
		- 多条命令重写为一条

	- 触发策略

		- 手动触发

			- bgrewriteaof命令，fork子进程

		- 自动触发

			- 当前AOF文件满足两个参数

				- auto-aof-rewrite-min-size

					- 重写时文件最小体积，默认64MB

				- auto-aof-rewrite-percentage

					- 重写时当前AOF和上次重写的AOF文件大小比值

	- 重写流程

		- 确保当前未执行save/bgsave/bgrewriteaof
		- 父进程阻塞请求，fork子进程
		- 父进程恢复响应

			- 写命令追加到原AOF缓冲区和AOF重写缓冲区
			- 子进程根据父进程内存快照，生成写命令同步到新AOF文件

		- 子进程发送完成信号给父进程，父进程更新统计信息
		- 父进程将AOF重写缓冲区数据写入新AOF文件
		- 新AOF文件替换原AOF文件，完成AOF重写

### AOF文件

- AOF开启时，Redis启动有限加载AOF
- APOF文件校验

	- 文件损坏则Redis启动失败
	- 文件结尾不完整，若开启aof-load-truncated，则日志输出警告，Redis启动成功

### 常用配置

- appendonly no

	- 是否开启AOF

- appendfilename "appendonly.aof"

	- AOF文件名

- dir ./

	- RDB/AOF文件所在目录

- appendfsync everysec

	- 文件同步(fsync)策略

- no-appendfsync-on-rewrite no

	- AOF重写期间是否禁止文件同步(fsync)

- auto-aof-rewrite-percentage 100

	- 文件重写触发参数

- auto-aof-rewrite-min-size 64mb

	- 文件重写触发参数

- aof-load-truncated yes

	- Redis启动是否载入结尾损坏的AOF文件

## 4.方案选择

### 优缺点

- RDB

	- 文件紧凑，体积小，全量恢复快，性能影响小
	- 无法实时持久化，文件格式特定，兼容性差

- AOF

	- 文件大，恢复速度慢，性能影响大
	- 支持秒级持久化，兼容性好

### 持久化策略参考

- Redis中的数据可完全丢弃

	- 关闭持久化

- 单机环境，可接受数据丢失

	- 分钟级，RDB
	- 秒级，AOF

- 主从环境

	- Master关闭持久化，性能最佳
	- Slave，关闭RDB，开启AOF，定时备份和重写AOF

- 异地容灾

	- 定时将RDB/重写的AOF文件拷贝到远程机器

## 5.相关问题

### fork阻塞，CPU阻塞

- 现象

	- fork的子进程不复制父进程的数据空间，但会复制内存页表(内存的索引/目录)
	- 父进程数据空间越大，内存页表就越大，fork耗时约多

- 解决方案

	- 控制Redis单机内存大小(10G)，可减轻fork阻塞

### AOF追加阻塞，硬盘阻塞

- 现象

	- 硬盘负载过高，fsync速度太慢，造成超过1s的数据丢失

- 解决方案

	- Redis主进程进行AOF时比对上次fsync时间，若超过2s则阻塞等待fsync完成

### 持久化问题定位

- info Persistence

	- rdb_last_bgsave_status，上次bgsave执行结果
	- rdb_last_bgsave_time_sec，上次bgsave执行耗时
	- aof_enabled，AOF是否开启
	- aof_last_rewrite_time_sec，上次AOF重写执行时间
	- aof_last_bgrewrite_status，上次bgwrite执行结果
	- aof_buffer_length/aof_rewrite_buffer_length，AOF缓存区和AOF重写缓存区大小
	- aof_delayed_fsync，AOF追加阻塞情况统计

- info stats

	- latest_fork_usec，上次fork耗时

*XMind - Trial Version*