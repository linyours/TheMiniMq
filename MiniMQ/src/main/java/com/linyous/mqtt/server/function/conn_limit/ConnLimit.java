package com.linyous.mqtt.server.function.conn_limit;

import com.linyous.mqtt.spi.annotation.MiniSPI;

/**
 * @author Linyous
 * @date 2021/6/21 16:29
 */
@MiniSPI
public interface ConnLimit {
    boolean isBeyondLimit();
}
