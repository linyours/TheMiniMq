package com.linyous.mqtt.api.client.handler;

import com.linyous.mqtt.api.common.Cache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Linyous
 * @date 2021/6/24 15:22
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Cache.channelHandlerContext = ctx;
        System.out.println("连接成功");
    }
}
