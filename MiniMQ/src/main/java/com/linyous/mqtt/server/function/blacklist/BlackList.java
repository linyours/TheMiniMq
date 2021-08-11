package com.linyous.mqtt.server.function.blacklist;

import com.linyous.mqtt.spi.annotation.MiniSPI;

/**
 * @author Linyous
 * @date 2021/6/21 17:38
 */
@MiniSPI
public interface BlackList {
    boolean isBlack(String id);

    void addBlack(String id);
}
