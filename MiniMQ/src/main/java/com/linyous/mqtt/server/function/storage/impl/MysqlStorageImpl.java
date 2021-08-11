package com.linyous.mqtt.server.function.storage.impl;

import com.linyous.mqtt.server.entity.Message;
import com.linyous.mqtt.server.function.storage.Storage;

/**
 * @author Linyous
 * @date 2021/6/21 15:19
 */
public class MysqlStorageImpl implements Storage {
    @Override
    public void store(Message message) {
        System.out.println("Mysql方式存储消息:" + message);
    }
}
