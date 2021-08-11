package com.linyous.mqtt.api.client;

import com.linyous.mqtt.api.client.handler.ClientChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttEncoder;

/**
 * @author Linyous
 * @date 2021/6/24 15:19
 */
public class NettyClient {
    public static void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                            pipeline.addLast(new ClientChannelHandler());
                        }

                    });
            /**客户端向服务器发起连接请求
             * 1-ChannelConfig由ChannelOption初始化
             * 2-ChannelPipeline(默认DefaultChannelPipeline)添加ChannelHandler
             * 3-注册Channel并添加监听器ChannelFutureListener.CLOSE_ON_FAILURE
             * 以异步的方式等待上述操作的完成
             * */
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 7777).sync();
            channelFuture.channel().closeFuture().sync();
            System.out.println("client close sucess");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
