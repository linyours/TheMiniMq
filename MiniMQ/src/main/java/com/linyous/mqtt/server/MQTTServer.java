package com.linyous.mqtt.server;


import com.linyous.mqtt.server.handler.MQTTChannelInitializer;
import com.linyous.mqtt.server.thread.DealPublicMessageThread;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;

/**
 * @author Linyous
 * @date 2021/6/18 16:56
 */
public class MQTTServer {

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workGroup = null;
    private Channel channel = null;

    private static MQTTServer mqttServer = new MQTTServer();

    public static MQTTServer getInstance() {
        return mqttServer;
    }

    private MQTTServer() {

    }

    public ChannelFuture start(int port) {
        DealPublicMessageThread.INSTANCE.start();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(16);

        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new MQTTChannelInitializer())
                .option(ChannelOption.SO_BACKLOG, 2048)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);// 内存泄漏检测级别高级

        ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
        System.out.println("MQTT服务器已启动！！！-------------------------端口：" + port);

        channel = future.channel();
        return future;
    }

    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        MQTTServer mqttServer = MQTTServer.getInstance();
        mqttServer.start(7777);
    }
}
