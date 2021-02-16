## **第一章-Nginx安装**

#### **Nginx概述**

Nginx类似Apache/Lighttpd/Tomcat/Jetty，是Web服务器，具备REST架构风格，运用URI/URL资源描述与定位，采用HTTP协议

Nginx对比其他Web服务器有如下特点

* 性能高/速度快

  单次请求响应快，并发请求处理快

* 拓展性高

  由不同功能/层次/类型的低耦合模块组合，第三方可以充分利用官方模块开发个性化模块

* 可靠性高

  优秀、简单设计的核心框架代码

* 消耗低

  高并发连接消耗内存少

* 并发高

  单机支持10万以上的并发连接

* 热部署

  Master管理进程与Worker工作进程独立工作，支持不停止服务就更新配置

* BSD许可协议

  允许用户自由使用或修改Nginx源码



#### **Nginx使用前置**

如何在Linux上运行Nginx

##### **Linux操作系统内核版本**

大于2.6才支持epoll，select/poll解决事件的多路复用策略无法解决高并发压力，可用uname -a查看内核版本

##### **支持软件**

支持Nginx常用功能的必备软件

* GCC编译器

  编译Nginx原代码工具，可用yum install -y gcc安装

  * G++编译器(开发Nginx)

    运用C++编写Nginx HTTP模块工具，可用yum install -y gcc-c++

* PCRE库

  正则表达式函数库，可用yum install -y pcre pcre-devel安装

  * pcre-devel(开发Nginx)

* zlib库

  对HTTP包内容做gzip格式的压缩，可用yum install -y zlib zlib-devel安装

  * zlib-devel(开发Nginx)

* OpenSSL

  支持HTTPS、MD5/SHA1散列函数，可用yum install -y openssl openssl-devel

##### **磁盘目录**

* Nginx源代码目录

* Nginx编译中间文件目录(objs)

  存放configure命令生成的源文件、make命令生成的目标文件、最终连接成功的二进制文件

* 部署目录(/usr/local/nginx)

  Nginx运行期间所需的二进制文件、配置文件等

* 日志目录

  Nginx的debug级别日志

##### **Linux内核参数设置(/etc/sysctl.conf)**

设置参数支持高并发访问，Nginx作为静态Web服务器、反向代理服务器、图片缩略图服务器参数设置不同

通用Nginx支持多TCP并发请求的参数调优如下，执行sysctl -p生效参数设置

```
fs.file-max = 999999   # 进程同时打开最大句柄数(最大并发连接数)

net.ipv4.tcp_tw_reuse = 1   # 允许TIME-WAIT的socket重新用于新的TCP连接 
net.ipv4.tcp_keepalive_time = 600  # TCP发送keepalive消息的频率(减少清理无效连接)
net.ipv4.tcp_fin_timeout = 30   # 服务器关闭连接socket保持FIN-WAIT-2状态的最大时间
net.ipv4.tcp_max_tw_buckets = 5000   # 允许TIME_WAIT套接字数量的最大值(减少提升Web服务器性能)
net.ipv4.ip_local_port_range = 1024    61000 # UDP和TCP连接中本地端口取值范围
net.ipv4.tcp_rmem = 4096 32768 262142   # TCP接收缓存的最小值、默认值、最大值
net.ipv4.tcp_wmem = 4096 32768 262142   # TCP发送缓存的最小值、默认值、最大值
net.core.netdev_max_backlog = 8096   # 保存网卡多余接收数据包的队列最大值

net.core.rmem_default = 262144   # 内核套接字接收缓存区默认的数值
net.core.wmem_default = 262144   # 内核套接字发送缓存区默认的数值
net.core.rmem_max = 2097152   # 内核套接字接收缓存区最大的数值
net.core.wmem_max = 2097152   # 内核套接字发送缓存区默认的大小

net.ipv4.tcp_syncookies = 1   # 解决TCP的SYN公机
net.ipv4.tcp_max_syn.backlog=1024  # TCP握手建立接受SYN请求队列的最大长度(增大不丢失连接请求)
```



#### **Nginx下载与安装**

##### **源代码下载**

[Nginx官网](http://nginx.org/en/download.html)下载源代码包，如稳定版(Stable Version)的nginx-1.18.0.tar.gz

放置在Nginx源代码目录中解压，tar -zxvf nginx-1.18.0.tar.gz

##### **编译安装**

在Nginx源代码目录执行以下命令

* configure命令

  检测操作系统内核和支持软件，参数解析，生成中间目录文件

  支持指定路径、编译、支持软件、模块等相关的参数

* make命令

  根据configure命令生成的Makefile文件编译Nginx工程，生成目标文件、最终二进制文件

* make install命令

  根据configure执行参数将Nginx部署到指定的安装目录

```bash
$ ./configure
$ make
$ make install
```



#### **Nginx命令行控制**

##### Nginx默认路径

* 安装目录，usrlocal/nginx/
* 二进制文件路径，usrlocal/nginx/sbin/nginx
* 配置文件路径，usrlocal/nginx/conf/nginx.conf

##### 运用Linux命令行控制Nginx服务器的行为

* 启动Nginx(执行二进制文件)，usrlocal/nginx/sbin/nginx
  * -c  tempnginx.conf，指定配置文件
  * -p usrlocal/nginx/，指定安装目录
  * -g "pid var nginx/test.pid"，指定全局配置项
* 测试与查看Nginx，usrlocal/nginx/sbin/nginx
  * -t，测试Nginx配置文件是否出错(不启动Nginx)
  * -t -q，不输出error级别错误
  * -v，显示Nginx版本信息 
  * -V，显示Nginx配置编译信息，如GCC编译器版本、Linux版本、configure命令参数等
* 停止Nginx，usrlocal/nginx/sbin/nginx
  * -s stop，快速停止
  * -s quit，处理完当前请求再停止
* 重启Nginx，usrlocal/nginx/sbin/nginx
  * -s reload，检查新配置项是否出错，无错则重启Nginx生效新配置
  * -s reopen，重写Nginx日志文件，可用于将旧日志备份、控制新日志文件大小
* 升级Nginx版本
  * 通知旧版本Nginx准备升级，kill -s SIGUSR2 <nginx master pid\>
  * 启动新版本Nginx
  * 停止旧版本Nginx





## **第二章-Nginx配置**

配置Nginx可以轻易添加官方或第三方发布的模块

#### **Nginx进程关系**

* Master进程

  单进程，管理Worker进程，提供命令行服务

* Worker进程

  多进程，数量与CPU核心数一致，并发处理请求提供服务，

#### **Nginx配置文件语法**

* 块配置项(配置名+大括号)
  * 可嵌套，内层块继承外层块的配置
  * 内外层块配置冲突取决于解析模块
* 配置项(配置项名+配置项值+分号)
  * 配置项名需符合Nginx规则
  * 配置项值
    * 多值用空格分隔，值含空格用单引号包含
    * 空间单位，可用K/M
    * 时间单位，可用ms/s/m/h/d/w/M/y
    * 变量可用$标识
* 注释(#)

```nginx
user  nobody; 
worker_processes  8; 
error_log  varlog/nginx/error.log error; 
#pid           logs/nginx.pid; 
events {    
    use epoll;    
    worker_connections  50000; 
} 
http {    
    include       mime.types;    
    default_type  application/octet-stream;    
    log_format  main  '$remote_addr [$time_local] "$request" '
                      '$status $bytes_sent "$http_referer" '                      
                      '"$http_user_agent" "$http_x_forwarded_for"';    
    access_log  logs/access.log  main buffer=32k;    
    …
｝
```

#### **Nginx基本配置**

Nginx运行加载的核心模块和事件类模块所支持的配置称为Nginx基本配置，其他模块执行均依赖这些配置项，大致可分为以下四类

##### **调试、定位问题的配置项**

* daemon on|off，默认on，是否开启守护进程
* master_process on|off，默认on，是否以Master/Worker工作模式
* error_log path level，默认error_log logs/error error，设置error日志的路径和级别
* events{debug_connection IP/CIDR}，设置该地址请求输出debug级别日志
  * configure --with-debug参数此配置才会生效
* worker_rlimit_core size，限制coredump核心转储文件大小
* worker_directory path，指定coredump核心转储文件生成目录

##### **正常运行的必备配置项**

* env VAR|VAR=VALUE，定义环境变量
* include path/file，将其他配置文件嵌入当前配置文件中
* pid path/file，默认logs/nginx.pid，设置保存Master进程ID的pid文件存放路径
* user username[groupname]，默认user nobody nobody，Master进程fork的Worker进程运行的用户组和用户
* worker_rlimit_nofile limit，设置单个Worker进程可以打开的最大句柄描述符个数
* worker_rlimit_sigpending limit，设置每个用户发往Nginx的信号队列大小

##### **优化性能的配置项**

* worker_processes number，默认1，设置Worker进程个数，一般为CPU内核个数
* worker_cpu_affinity [cpumask...]，绑定Worker进程到指定CPU内核，如4核则1000 0100 0010 0001
* ssl_engine device，配置SSL硬件加速设备
* worker_priority number，默认0，设置Wroker进程的nice优先级(CPU时间片)

##### **事件类配置项**

* accept_mutex on|off，默认on，是否打开Worker进程的负载均衡锁
* lock_file path/file，默认logs/nginx.lock，设置实现accept锁的文件锁路径
* accept_mutex_delay time，默认500ms，设置获取accept锁的间隔事件
* multi_accept on|off，默认off，设置是否批量建立新连接
* use kqueue|rtsig|epoll|dev/poll|select|poll|eventport，默认Nginx自动选择，设置选择事件模型
* worker_connections number，设置每个Worker进程可以同时处理的最大连接数

#### **Nginx配置静态Web服务器**

静态Web服务器功能主要由ngx_http_core_module模块提供

用户可通过在nginx.conf中配置此模块提供的配置项及变量，实现静态Web服务器功能

块配置项大致如下

* http块
* server块
* location块
* upstream块或if块

配置项从功能上分类如下

* 虚拟主机与请求的分发

  * server{listen ip:port [params]}

    默认80，设置监听端口，参数如下

    * default/default_server，将所在server块作为整个Web服务的默认server块
    * backlog=num，默认-1，设置TCP中backlog队列大小
    * rcvbuf=size，设置监听句柄的SO_RCVBUF参数
    * sndbuf=size，设置监听句柄的SO_SNDBUF参数
    * accept_filter，设置accept过滤器
    * deferred，设置内核在网卡中收到请求才唤起Worker进程处理连接，大并发可减轻Worker进程负担
    * bind ip:port，绑定当前端口/地址对
    * ssl，当前监听端口上建立的连接必须基于SSL协议

  * server{server_name name[...]}

    默认""，设置主机名称，用于匹配HTTP请求Header中Host

  * server/http/location{server_names_hash_bucket_size size}

    默认32|64"128，设置存储server name的散列桶占用的内存大小

  * server/http/location{ server_names_hash_max_size size}

    默认512，设置存储server name的散列桶的容量

  * server/http/location{server_name_in_redirect on|off}

    默认on，设置重定向请求时会不会使用server_name配置的主机名替换请求头部中的Host

  * server{ location[=|~|~*|^~|@]/uri/{...}}

    设置处理匹配以上规则的请求的location配置块

* 文件路径的定义

  * server/http/location/if{root path}

    默认html，设置匹配请求的资源的绝对路径，如匹配/download，真实匹配的路径是/path

  * location{alias path}

    设置匹配请求的资源的相对路径，如匹配/download，真实匹配的路径是/path/download

  * server/http/location{index file[...]}

    默认index.html，设置匹配/请求的首页文件

  * server/http/location/if{ error_page code\[code...][=|=answer-code]uri|@named_location}

    设置错误码重定向页面

  * server/http/location{ recursive_error_pages[on|off]}

    默认off，是否允许递归定义error_page

  * server/location{ try_files path1[path2]uri}

    设置访问路径，若均失效，则重定向到最后参数的URI

* 内存及磁盘资源的分配

  * server/http/location{ client_body_in_file_only on|clean|off}

    默认off，HTTP请求包体是否存储到磁盘文件

  * server/http/location{ client_body_buffer_size size}

    默认8K/16K，设置接收HTTP请求包体的内存缓冲区大小，过大则写入磁盘

  * server/http{ large_client_header_buffers number size}

    默认48K，设置接收HTTP请求Header部分的最大内存缓冲区大小，过大则返回414错误

  * server/http{ client_header_buffer_size size}

    默认1K，设置接收HTTP请求Header部分时分配的内存大小

  * server/http/location{ client_body_temp_path dir-path[level1[level2[level3]]]}

    默认 client_body_temp，设置HTTP包体存放的临时目录

  * server/http{connection_pool_size size}

    默认256，设置建立成功的TCP连接预先分配的内存池大小

  * server/http{request_pool_size size}

    默认4K，设置处理HTTP请求分配的内存池的初始大小

* 网络连接的设置

  * server/http/location{client_header_timeout time}

    默认60，设置建立连接后服务器读取HTTP头部的超时时间，超时则返回408

  * server/http/location{client_body_timeout 60}

    默认60，设置建立连接后服务器读取HTTP包体的超时时间

  * server/http/location{ send_timeout time}

    默认60，设置服务器发送数据包客户端的响应时间，超时则服务器关闭连接

  * server/http/location{reset_timeout_connection on|off}

    默认off，设置连接超时后服务器是否向客户端发送RST重置连接

  * server/http/location{ lingering_close off|on|always}

    默认on，设置Nginx关闭用户连接方式

  * server/http/location{ lingering_time time}

    默认30s，lingering_close启用后，服务端响应413后客户端仍能持续上传HTTP数据的时间

  * server/http/location{ lingering_timeout time}

    默认5s，lingering_close启用后，关闭连接前无数据可读的时间

  * server/http/location{keepalive_timeout time}

    默认75，限制keepalive连接的关闭时间

  * server/http/location{ keepalive_requests number}

    默认100，keepalive长连接上允许承载的请求最大数

  * server/http/location{ tcp_nodelay on|off}

    默认on，是否对keepalive连接使用TCP_NODELAY选项

* MIME类型的设置

  

* 对客户端请求的限制

* 文件操作的优化

* 对客户端请求的特殊处理

