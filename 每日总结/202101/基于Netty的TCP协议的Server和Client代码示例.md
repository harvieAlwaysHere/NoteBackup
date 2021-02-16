#### **Netty依赖包**

```xml
<dependency>
	<groupId>io.netty</groupId>
	<artifactId>netty-all</artifactId>
	<version>4.1.52.Final</version>
</dependency>
```

#### **TCP Server**

Bootstrap配置参数

* group()，设置线程组
* channel()，设置服务端通道(ServerSocketChannel)实现类，用于Netty反射生成
* option()，配置ServerSocketChannel
  * ChannelOption.SO_BACKLOG，设置等待队列容量，客户端连接请求速率大于NioServerSocketChannel会缓存至该队列
* childOption()，配置ServerSocketChannel接收的SocketChannel
  * ChannelOption.SO_KEEPALIVE，设置连接保活
* childHandler()，设置WorkerGroup的业务处理器
  * ChannelInitializer，通道初始化对象
  * initChannel()，通道初始化方法
    * socketChannel.pipeline().addLast()，添加业务处理器(Handler)

```java
public static void main(String[] args) throws InterruptedException {

    // 创建 BossGroup(连接请求) 和 WorkerGroup(读写请求)
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
        // 创建 Bootstrap 服务器端启动对象
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 配置参数
        bootstrap
            .group(bossGroup, workerGroup)  
            .channel(NioServerSocketChannel.class)  
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(
            	new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(new NettyServerHandler());
                    }
                });

        System.out.println("server is ready...");

        // 绑定端口 启动服务器 生成channelFuture对象(Netty异步模型)
        ChannelFuture channelFuture = bootstrap.bind(8080).sync();
        // 对通道关闭进行监听
        channelFuture.channel().closeFuture().sync();
    } finally {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}

/**
 * 自定义一个 Handler，需要继承Netty规范下的某个HandlerAdapter
 * InboundHandler 用于处理数据流入本端的IO事件
 * OutboundHandler 用于处理数据流出本端的IO事件
 */
static class NettyServerHandler extends ChannelInboundHandlerAdapter {
    
    /**
     * 读取通道数据并解析处理
     * @param ctx 上下文对象 包含关联的Pipeline/Channel/客户端地址等
     * @param msg 客户端发送的(通道)数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        
        // 获取客户端地址
        System.out.println("client address: " + ctx.channel().remoteAddress());

        // 读取通道数据并解析处理
        // ByteBuf(Netty) 性能高于 ByteBuffer(JDK NIO)
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("data from client: " + byteBuf.toString(CharsetUtil.UTF_8));
    }

    // 数据读取完毕后执行方法
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        
        // 发送响应给客户端
        // Unpooled(Netty)操作缓冲区 copiedBuffer返回高性能的Netty封装的ByteBuf
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client! i have got your data.",CharsetUtil.UTF_8));
        
    }

    
    // 异常时执行方法
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭与客户端的Socket连接
        ctx.channel().close();
    }
}

```

#### **TCP Client**

Bootstrap配置参数

* group()，设置线程组
* channel()，设置客户端通道(SocketChannel)实现类，用于Netty反射生成
* handler()，设置BossGroup的业务处理器

```java
public static void main(String[] args) throws InterruptedException {

    // 客户端的事件循环组 可看做BossGroup
    EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    try {
        // 创建客户端的启动对象
        Bootstrap bootstrap = new Bootstrap();
        // 配置参数
        bootstrap
            .group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(
            	new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });

        System.out.println("client is ready...");

        // 启动客户端 连接服务器端(IP+PORT) 生成channelFuture对象(Netty异步模型)
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1",8080).sync();
        // 对通道关闭进行监听
        channelFuture.channel().closeFuture().sync();
    } finally {
        eventLoopGroup.shutdownGracefully();
    }
}

static class NettyClientHandler extends ChannelInboundHandlerAdapter {
    
    // 通道就绪时执行方法
    @Override
    public void channelActive(ChannelHandlerContext ctx)throws Exception {
        
        // 向服务器发送数据
        // Unpooled(Netty)操作缓冲区 copiedBuffer返回高性能的Netty封装的ByteBuf
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello server!",CharsetUtil.UTF_8));
        
    }

    /**
     * 读取通道数据并解析处理
     * @param ctx 上下文对象
     * @param msg 服务器端发送的(通道)数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)throws Exception {
        
        // 获取服务端地址
        System.out.println("server address: "+ ctx.channel().remoteAddress());

        // 读取通道数据并解析处理
        // ByteBuf(Netty) 性能高于 ByteBuffer(JDK NIO)
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("data from server: "+ byteBuf.toString(CharsetUtil.UTF_8));
        
    }

    // 异常时执行方法
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭与服务器端的 Socket 连接
        ctx.channel().close();
    }
}

```

