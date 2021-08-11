package com.linyous.mqtt.server.function.storage;

import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.server.entity.Message;
import com.linyous.mqtt.spi.MiniExtensionLoader;

/**
 * @author Linyous
 * @date 2021/6/21 15:44
 */
public enum StorageManager {

    INSTANCE;

    private Storage storage;

    StorageManager() {
        this.storage = MiniExtensionLoader.getExtensionLoader(Storage.class).getExtension(Status.STORAGE_WAY);
    }

    public void store(Message message) {
        this.storage.store(message);
    }
}
