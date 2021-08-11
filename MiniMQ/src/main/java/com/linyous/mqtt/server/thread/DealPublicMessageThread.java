package com.linyous.mqtt.server.thread;

import com.linyous.mqtt.server.common.Cache;
import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.server.entity.ClientInfo;
import com.linyous.mqtt.server.entity.Message;
import com.linyous.mqtt.server.function.storage.StorageManager;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Linyous
 * @date 2021/6/20 14:49
 */
public enum DealPublicMessageThread {

    INSTANCE;

    public DealPublicMessageThread getInstance() {
        return INSTANCE;
    }

    public void start() {
        for (int i = 0; i < Status.MAX_DEAL_PUB_MESSAGE_NUM; i++) {
            System.out.println("线程" + i + "启动...");
            new DealMessageThread().start();
        }
    }


    private class DealMessageThread extends Thread {
        @Override
        public void run() {
            while (true) {
                Message message = null;
                try {
                    message = Cache.MESSAGE_QUEUE.take();
                } catch (InterruptedException e) {
                    continue;
                }
                System.out.println("收到消息:" + message);
                switch (message.getMqttQoS()) {
                    case AT_MOST_ONCE:
                        dealMessageQoS0(message);
                        break;
                    case AT_LEAST_ONCE:
                        dealMessageQoS1(message);
                        break;
                    case EXACTLY_ONCE:
                        dealMessageQoS2(message);
                        break;
                }
            }
        }

        /**
         * 处理QoS为0的消息
         *
         * @param message
         */
        private void dealMessageQoS0(Message message) {
            try {
                dealMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 处理QoS为1的消息
         *
         * @param message
         */
        private void dealMessageQoS1(Message message) {
            try {
                dealMessage(message);
            } catch (Exception e) {
                //出现异常，需要进行记录重试，这里简单的吧消息重新放回队列就完事了
                try {
                    Cache.MESSAGE_QUEUE.put(message);
                    return;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            //移除发送中缓存
            Cache.MESSAGE_ID_CACHE.remove(message.getMessageId());
            //处理完成需要发送响应给发送端
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(message.getMessageId());
            MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(fixedHeader, variableHeader);
            message.getCtx().writeAndFlush(mqttPubAckMessage);
        }

        /**
         * 处理QoS为2的消息
         *
         * @param message
         */
        private void dealMessageQoS2(Message message) {
            try {
                dealMessage(message);
            } catch (Exception e) {
                //出现异常，需要进行记录重试，这里简单的吧消息重新放回队列就完事了
                try {
                    Cache.MESSAGE_QUEUE.put(message);
                    return;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            //储存没有收到pubdel的消息
            Cache.MESSAGE_NOT_GET_PUB_DEL.put(message.getMessageId(), message);
            //处理完成需要发送响应给发送端
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(message.getMessageId());
            MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(fixedHeader, variableHeader);
            message.getCtx().writeAndFlush(mqttPubAckMessage);
        }

        private void dealMessage(Message message) throws Exception {
            //储存消息
            StorageManager.INSTANCE.store(message);
            publishMessage(message.getTopic(), message.getPayload());
        }

        /**
         * 发送给所有订阅该topic的客户端
         *
         * @param topic
         * @param payload
         */
        private void publishMessage(String topic, byte[] payload) {
//            System.out.println("发送消息了 ?? " + payload);
            System.out.println("发送消息了 ?? " + Arrays.toString( payload));
            List<String> ipList = Cache.TOPIC_MAP.get(topic);
            //这里没有处理broker发往客户端消息的QoS
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(0), false, 0);
            MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic, Status.MESSAGE_ID.getAndIncrement());
            MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    mqttFixedHeader,
                    mqttPublishVariableHeader,
                    Unpooled.buffer().writeBytes(payload)
            );
            for (String s : ipList) {
                ClientInfo clientInfo = Cache.CLIENT_MAP.get(s);
                clientInfo.getChannelHandlerContext().writeAndFlush(publishMessage);
            }
        }
    }

}
