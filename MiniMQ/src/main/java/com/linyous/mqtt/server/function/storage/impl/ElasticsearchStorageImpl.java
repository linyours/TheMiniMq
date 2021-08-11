package com.linyous.mqtt.server.function.storage.impl;

import com.linyous.mqtt.server.entity.Message;
import com.linyous.mqtt.server.function.storage.Storage;

/**
 * @author Linyous
 * @date 2021/6/21 15:20
 */
public class ElasticsearchStorageImpl implements Storage {
    @Override
    public void store(Message message) {
        System.out.println("Elasticsearch方式存储消息:" + message);
    }
}
