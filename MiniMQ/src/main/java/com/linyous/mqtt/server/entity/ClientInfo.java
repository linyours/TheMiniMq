package com.linyous.mqtt.server.entity;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author Linyous
 * @date 2021/6/18 17:52
 */
public class ClientInfo {
    private String clientIdentifier;
    private ChannelHandlerContext channelHandlerContext;

    public ClientInfo() {
    }

    public ClientInfo(String clientIdentifier, ChannelHandlerContext channelHandlerContext) {
        this.clientIdentifier = clientIdentifier;
        this.channelHandlerContext = channelHandlerContext;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "clientIdentifier='" + clientIdentifier + '\'' +
                ", channelHandlerContext=" + channelHandlerContext +
                '}';
    }
}
