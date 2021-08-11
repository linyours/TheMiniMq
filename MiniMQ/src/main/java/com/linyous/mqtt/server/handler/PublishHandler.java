package com.linyous.mqtt.server.handler;

import com.linyous.mqtt.server.common.Cache;
import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.server.entity.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;

import static io.netty.handler.codec.mqtt.MqttQoS.*;

/**
 * @author Linyous
 * @date 2021/6/18 17:03
 */
public class PublishHandler extends ChannelInboundHandlerAdapter implements BaseHandlerInterface {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MqttMessage) {
            MqttMessage message = (MqttMessage) msg;
            MqttMessageType messageType = message.fixedHeader().messageType();
            switch (messageType) {
                case PUBLISH:
                    // 客户端发布普通消息
                    MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) msg;
                    pub(ctx, mqttPublishMessage);
                    break;
                case PUBREL:
                    // 客户端发布释放
                    MqttPublishMessage mqttPublishMessage2 = (MqttPublishMessage) msg;
                    com(mqttPublishMessage2);
                    break;
                default:
                    ctx.fireChannelRead(msg);
                    break;
            }
        } else {

        }
    }

    /**
     * 发布消息
     *
     * @param ctx
     * @param mqttPublishMessage
     */
    private void pub(ChannelHandlerContext ctx, MqttPublishMessage mqttPublishMessage) {
        String ip = getIpAddress(ctx);
        System.out.println("客户端" + ip + "请求发布消息...");
        MqttQoS mqttQoS = mqttPublishMessage.fixedHeader().qosLevel();
        if (FAILURE == mqttQoS) {
            return;
        }
        synchronized (Cache.MESSAGE_ID_CACHE) {
            if (Cache.MESSAGE_ID_CACHE.containsKey(mqttPublishMessage.variableHeader().messageId())) {
                return;
            }
            if (mqttQoS == AT_LEAST_ONCE || mqttQoS == EXACTLY_ONCE) {
                Cache.MESSAGE_ID_CACHE.put(mqttPublishMessage.variableHeader().messageId(), Status.OBJECT);
            }
        }
        try {
            Cache.MESSAGE_QUEUE.put(getMessage(ctx, mqttPublishMessage));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布完成
     *
     * @param mqttPublishMessage
     */
    private void com(MqttPublishMessage mqttPublishMessage) {
        System.out.println("收到发布释放的消息...");
        int messageId = mqttPublishMessage.variableHeader().messageId();
        Cache.MESSAGE_ID_CACHE.remove(messageId);
        Message message = Cache.MESSAGE_NOT_GET_PUB_DEL.get(messageId);
        Cache.MESSAGE_NOT_GET_PUB_DEL.remove(messageId);
        //处理完成需要发送响应给发送端
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(fixedHeader, variableHeader);
        message.getCtx().writeAndFlush(mqttPubAckMessage);
    }

    private Message getMessage(ChannelHandlerContext ctx, MqttPublishMessage mqttPublishMessage) {
        return new Message(mqttPublishMessage.variableHeader().messageId(),
                mqttPublishMessage.variableHeader().topicName(),
                "mqttPublishMessage.payload().array()".getBytes(),
                mqttPublishMessage.fixedHeader().qosLevel(),
                ctx);
    }

}
