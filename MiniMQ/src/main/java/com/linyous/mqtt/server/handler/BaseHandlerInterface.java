package com.linyous.mqtt.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

/**
 * @author Linyous
 * @date 2021/6/20 10:16
 */
public interface BaseHandlerInterface {
    default String getIpAddress(ChannelHandlerContext ctx) {
        //拿到客户端与服务器连接的channel
        Channel channel = ctx.channel();
        //客户端的网络信息 包括host，ip，port等信息
        InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
        //获取终端的host name
        String host = insocket.getHostName();
        //格式化IP和port的格式
        String clientIpAddress = (host.equals("localhost") ? "127.0.0.1" : host) + ":" + insocket.getPort();
        return clientIpAddress;
    }
}
