package com.linyous.mqtt.server.function.conn_limit;

import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.spi.MiniExtensionLoader;

/**
 * @author Linyous
 * @date 2021/6/21 15:44
 */
public enum ConnLimitManager {

    INSTANCE;

    private ConnLimit connLimit;

    ConnLimitManager() {
        this.connLimit = MiniExtensionLoader.getExtensionLoader(ConnLimit.class).getExtension(Status.LIMIT_WAY);
    }

    public boolean isBeyondLimit() {
        return this.connLimit.isBeyondLimit();
    }
}
