package com.linyous.mqtt.api.service;

import com.linyous.mqtt.api.annotation.APIClass;
import com.linyous.mqtt.api.common.Cache;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;

/**
 * @author Linyous
 * @date 2021/6/24 11:10
 */
@APIClass("publishMessageService")
public class PublishMessageService {
    public void publish(String topic, String message) {
        //这里没有处理broker发往客户端消息的QoS
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(0), false, 0);
        MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic, 0);
        MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                mqttFixedHeader,
                mqttPublishVariableHeader,
                Unpooled.buffer().writeBytes(message.getBytes())
        );
        Cache.channelHandlerContext.writeAndFlush(publishMessage);
    }
}
