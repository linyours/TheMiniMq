package com.linyous.mqtt.server.function.storage;

import com.linyous.mqtt.server.entity.Message;
import com.linyous.mqtt.spi.annotation.MiniSPI;

/**
 * @author Linyous
 * @date 2021/6/21 15:18
 */
@MiniSPI
public interface Storage {
    public void store(Message message);
}
