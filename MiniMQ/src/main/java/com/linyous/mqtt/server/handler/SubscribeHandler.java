package com.linyous.mqtt.server.handler;

import com.linyous.mqtt.server.common.Cache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Linyous
 * @date 2021/6/18 17:03
 */
public class SubscribeHandler extends ChannelInboundHandlerAdapter implements BaseHandlerInterface {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof MqttMessage) {
            MqttMessage message = (MqttMessage) msg;
            MqttMessageType messageType = message.fixedHeader().messageType();

            switch (messageType) {
                case SUBSCRIBE:
                    MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) message;
                    sub(ctx, subscribeMessage);
                    break;
                case UNSUBSCRIBE:
                    break;
                default:
                    ctx.fireChannelRead(msg);
                    break;
            }
        } else {

        }
    }

    /**
     * 订阅操作 向服务器订阅所有感兴趣的主题 并且按照客户端标识找到未读消息接受这些消息
     *
     * @param ctx
     * @param subscribeMessage
     */
    private void sub(ChannelHandlerContext ctx, MqttSubscribeMessage subscribeMessage) {
        //拿到ip和port地址
        String ipAddress = getIpAddress(ctx);
        System.out.println("客户端" + ipAddress + "请求订阅...");
        List<MqttTopicSubscription> list = subscribeMessage.payload().topicSubscriptions();
        List<String> topNames = new ArrayList<String>();
        for (MqttTopicSubscription subscription : list) {
            topNames.add(subscription.topicName());
        }
        if (!topNames.isEmpty()) {
            for (String topName : topNames) {
                System.out.println(topName);
                List<String> ipList = Cache.TOPIC_MAP.get(topName);
                if (!ipList.contains(ipAddress)) ipList.add(ipAddress);
            }
        }
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttSubAckPayload payload = new MqttSubAckPayload(2);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(subscribeMessage.variableHeader().messageId());
        MqttSubAckMessage subAckMessage = new MqttSubAckMessage(fixedHeader, mqttMessageIdVariableHeader, payload);
        ctx.writeAndFlush(subAckMessage);
    }

    /**
     * 取消订阅操作
     *
     * @param ctx
     * @param mqttUnsubscribeMessage
     */
    private void unSub(ChannelHandlerContext ctx, MqttUnsubscribeMessage mqttUnsubscribeMessage) {
        //拿到ip和port地址
        String ipAddress = getIpAddress(ctx);
        System.out.println("客户端" + ipAddress + "取消订阅...");
        //要取消订阅的topic列表
        List<String> topNames = mqttUnsubscribeMessage.payload().topics();
        if (!topNames.isEmpty()) {
            for (String topName : topNames) {
                List<String> ipList = Cache.TOPIC_MAP.get(topName);
                if (ipList.contains(ipAddress)) ipList.remove(ipAddress);
            }
        }
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(mqttUnsubscribeMessage.variableHeader().messageId());
        MqttUnsubAckMessage unsubAckMessage = new MqttUnsubAckMessage(fixedHeader, mqttMessageIdVariableHeader);
        ctx.writeAndFlush(unsubAckMessage);
    }
}
