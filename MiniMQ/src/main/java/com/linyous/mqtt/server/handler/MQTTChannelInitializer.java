package com.linyous.mqtt.server.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * @author Linyous
 * @date 2021/6/18 17:02
 */
public class MQTTChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 读超时处理
        pipeline.addLast(new ReadTimeoutHandler(120));
        // 写超时处理
        pipeline.addLast(new WriteTimeoutHandler(120));
        pipeline.addLast(MqttEncoder.INSTANCE);
        pipeline.addLast(new MqttDecoder());
        pipeline.addLast(new ConnectionHandler());
        pipeline.addLast(new SubscribeHandler());
        pipeline.addLast(new PublishHandler());
        pipeline.addLast(new ExceptionHandler());
    }

}
