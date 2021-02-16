# Redis内存模型

## 1.内存统计

### used_memory，Redis分配的内存总量

### used_memory_rss，Redis进程占用内存总量

### mem_fragmentation_ratio，内存碎片比率

### mem_allocator，Redis内存分配器

## 2.内存划分

### used_memory

- 数据
- 缓冲内存

### used_memory_rss

- 进程运行内存

	- Redis主进程，如代码、常量池等
	- Redis子进程，如AOF/RDB创建的

- 内存碎片

## 3.数据存储

### DictEntry(24)，存储Key-Value指针

- Key*，指向存储Key值的SDS
- Value*，指向存储Value的RedisOvbject
- Next*，指向下一个DictEntry

### RedisObject(16)，存储Vlaue对象

- type(4)，对象类型
- encoding(4)，对象内部编码
- lru(24)，对象最后一次被命令程序访问的时间
- refcount(int)，对象被引用的次数

	- 共享对象(整数值字符串对象)

- ptr，指向存储数据值的SDS

### Jemalloc，内存分配器

### SDS(Simple Dynamic String)(9+)，存储字符串

- 数据结构

	- buf(char[])，存储字符串
	- len(int)，buf已使用长度
	- free(int)，buf未使用长度

- 优点

	- 获取长度O(1)
	- 杜绝缓冲区溢出
	- 减少内存重分配
	- len标识字符串结束，因此可以存储二进制数据

## 4.对象类型与内部编码

### 字符串(String)

- long，8字节长整型字符串
- embstr，长度小于39字节的字符串

	- 内存连续，一次分配
	- 只读

- raw，长度大于39字节的字符串
- 编码转换，long/embstr->raw

### 列表(List)

- ziplist，压缩列表

	- 一系列特殊编码的连续内存块组成的顺序型数据结构

- linkedlist，双端链表

	- 一个list结构(表头指针、表尾针织、列表长度、dup/free/match函数)
	- 多个listNode结构(向前指针、向后指针、RedisObject指针)

- 编码转换，ziplist->linkedlist

### 哈希(Hash)

- ziplist，压缩列表
- hashtable，哈希表

	- dictEntry，存储Key-Value指针
	- Bucket，存储指向DicEntry结构的指针数组
	- dictht

		- table，指向Bucket
		- size，Bucket大小
		- used，已使用的dictEntry数量
		- sizemask，size-1

	- dict

		- type+privdata，适应不同类型的键值对
		- ht，存储两个dictht的数组，用于rehash

- 编码转换，ziplist->hashtable

### 集合(Set)

- intset，整数集合
- hashtable，哈希表
- 编码转换，intset->hashtable

### 有序集合(Zset)

- ziplist，压缩列表
- skiplist，跳跃表

	- 有序数据结构，每个节点中维持多个指向其他节点的指针达到快速访问节点的目的
	- zskiplist+zskiplistNode组成

- 编码转换，ziplist->skiplist

## 5.应用举例

### 根据数据量估算Redis内存使用量

- 例如9W个非整型键值对

	- 判断字符串编码，7字节->embstr
	- DictEntry(80字节)

		- dictEntry，24字节，分配32字节内存
		- key(SDS)，9+7字节，分配16字节内存
		- redisObject，16字节，分配16字节内存
		- value(SDS)，9+7字节，分配16字节内存

	- Bucket数组(131072*8字节)

		- 数组大小，大于且最接近9W的2^N的数，131072
		- 每个数组元素为指针(8字节)

	- 总体，80*9W+131072*8字节

### 优化内存占用

- Jemalloc特性优化

	- 合理分配字符串长度，SDS+字符串长度达到小于且接近2^N

- 多采用整型/长整型(8字节)代替SDS存储
- 利用共享对象特性

### 关注内存碎片率

- \>1.03，内存碎片多
- 可重启Redis服务，重排内存数据
	
- <1，内存不足
- 运用了虚拟内存，性能慢
	- 可提高单机内存或增加服务器节点数量
	- 可减少Redis数据
	
	- 选择合适数据类型
		- 利用共享对象
		- 设置合理的数据回收策略

*XMind - Trial Version*