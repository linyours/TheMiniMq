package com.linyous.mqtt.server.function.blacklist;

import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.spi.MiniExtensionLoader;

/**
 * @author Linyous
 * @date 2021/6/21 15:44
 */
public enum BlackListManager {

    INSTANCE;

    private BlackList blackList;

    BlackListManager() {
        this.blackList = MiniExtensionLoader.getExtensionLoader(BlackList.class).getExtension(Status.BLACK_LIST_WAY);
    }

    public boolean isBlack(String id) {
        return this.blackList.isBlack(id);
    }

    public void addBlack(String id) {
        this.blackList.addBlack(id);
    }
}
