package com.linyous.mqtt.server.entity;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.Arrays;

/**
 * @author Linyous
 * @date 2021/6/20 14:20
 */
public class Message {
    private int messageId;
    private String topic;
    private byte[] payload;
    private MqttQoS mqttQoS;
    //发送该消息的客户端channel
    private ChannelHandlerContext ctx;

    public Message() {
    }

    public Message(int messageId, String topic, byte[] payload, MqttQoS mqttQoS, ChannelHandlerContext ctx) {
        this.messageId = messageId;
        this.topic = topic;
        this.payload = payload;
        this.mqttQoS = mqttQoS;
        this.ctx = ctx;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public MqttQoS getMqttQoS() {
        return mqttQoS;
    }

    public void setMqttQoS(MqttQoS mqttQoS) {
        this.mqttQoS = mqttQoS;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", topic='" + topic + '\'' +
                ", payload=" + Arrays.toString(payload) +
                ", mqttQoS=" + mqttQoS +
                ", ctx=" + ctx +
                '}';
    }
}
