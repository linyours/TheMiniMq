package com.linyous.mqtt.server.handler;

import com.linyous.mqtt.server.common.Cache;
import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.server.entity.ClientInfo;
import com.linyous.mqtt.server.function.blacklist.BlackListManager;
import com.linyous.mqtt.server.function.conn_limit.ConnLimitManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;

import java.util.LinkedList;

/**
 * @author Linyous
 * @date 2021/6/18 17:03
 */
public class ConnectionHandler extends ChannelInboundHandlerAdapter implements BaseHandlerInterface {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MqttMessage) {

            MqttMessage message = (MqttMessage) msg;
            MqttMessageType messageType = message.fixedHeader().messageType();

            switch (messageType) {
                case CONNECT:
                    try {
                        MqttConnectMessage connectMessage = (MqttConnectMessage) message;
                        ack(ctx, connectMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case PINGREQ:
                    ping(ctx);
                    break;
                case DISCONNECT:
                    loginout(ctx);
                    break;
                default:
                    ctx.fireChannelRead(msg);
                    break;
            }
        } else {

        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        try {
            super.channelReadComplete(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理连接请求
     *
     * @param ctx
     * @param connectMessage
     */
    private void ack(ChannelHandlerContext ctx, final MqttConnectMessage connectMessage) {
        String ipAddress = getIpAddress(ctx);
        System.out.println("客户端" + ipAddress + "请求连接...");
        if (BlackListManager.INSTANCE.isBlack(ipAddress) || ConnLimitManager.INSTANCE.isBeyondLimit()) {
            System.out.println(Status.LIMIT_TIME + "毫秒内客户端连接数超过" + Status.LIMIT_NUMBER + "，或者客户端已经被拉入黑名单");
            ctx.writeAndFlush(connResponseHeader(new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false,
                    0), MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE));
            return;
        }
        MqttConnectPayload connectPayload = connectMessage.payload();
        String ident = connectPayload.clientIdentifier();// clientId
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false,
                0);
        String userName = connectPayload.userName();
        String passWord = connectPayload.password();
        if (Status.CONN_COUNT.get() < Status.MAX_CONN_NUMS) {
            synchronized (Cache.CONN_LOCK) {
                if (Status.CONN_COUNT.get() < Status.MAX_CONN_NUMS) {
                    if (userName.equals(Status.AUTH_ID) && passWord.equals(Status.AUTH_SECRET)) {
                        Status.CONN_COUNT.getAndIncrement();
                        Cache.CLIENT_MAP.put(ipAddress, new ClientInfo(ident, ctx));
                        Cache.IDENT_IP_MAP.put(ident, ipAddress);
                        if (!Cache.TOPIC_MAP.containsKey("mini/topic/" + ident)) {
                            System.out.println("生成topic：" + "mini/topic/" + ident);
                            Cache.TOPIC_MAP.put("mini/topic/" + ident, new LinkedList<String>());
                        }
                        //返回连接成功的ACK
                        ctx.writeAndFlush(connResponseHeader(fixedHeader, MqttConnectReturnCode.CONNECTION_ACCEPTED));
                        return;
                    } else {
                        System.out.println("客户端" + ipAddress + "认证不通过...");
                        ctx.writeAndFlush(connResponseHeader(fixedHeader, MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED));
                        return;
                    }
                } else {
                    System.out.println("服务器已达到最大连接数...");
                    ctx.writeAndFlush(connResponseHeader(fixedHeader, MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE));
                    return;
                }
            }
        } else {
            System.out.println("服务器已达到最大连接数...");
            ctx.writeAndFlush(connResponseHeader(fixedHeader, MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE));
            return;
        }
    }

    /**
     * 心跳ping
     *
     * @param ctx
     */
    private void ping(ChannelHandlerContext ctx) {
        String ipAddress = getIpAddress(ctx);
//        System.out.println("客户端" + ipAddress + "发送心跳...");
        //构建心跳响应
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_LEAST_ONCE, false,
                0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader);
        ctx.channel().writeAndFlush(mqttMessage);
    }

    /**
     * 处理断开连接请求
     *
     * @param ctx
     */
    private void loginout(ChannelHandlerContext ctx) {
        String ipAddress = getIpAddress(ctx);
        System.out.println("客户端" + ipAddress + "断开连接...");
        Cache.IDENT_IP_MAP.remove(Cache.CLIENT_MAP.get(ipAddress).getClientIdentifier());
        Cache.CLIENT_MAP.remove(ipAddress);
        Status.CONN_COUNT.getAndDecrement();
        Channel channel = ctx.channel();
        channel.close();
    }

    //构建连接响应头部
    private MqttConnAckMessage connResponseHeader(MqttFixedHeader fixedHeader, MqttConnectReturnCode mqttConnectReturnCode) {
        MqttConnAckVariableHeader connectVariableHeader = new MqttConnAckVariableHeader(mqttConnectReturnCode, false);
        MqttConnAckMessage ackMessage = new MqttConnAckMessage(fixedHeader, connectVariableHeader);
        return ackMessage;
    }
}
