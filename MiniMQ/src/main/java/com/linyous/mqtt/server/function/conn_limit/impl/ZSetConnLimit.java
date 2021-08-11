package com.linyous.mqtt.server.function.conn_limit.impl;

import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.server.function.conn_limit.ConnLimit;
import com.linyous.mqtt.util.MiniZSet;

/**
 * @author Linyous
 * @date 2021/6/21 16:31
 */
public class ZSetConnLimit implements ConnLimit {

    private MiniZSet<Object> miniZSet;

    public ZSetConnLimit(){
        this.miniZSet = new MiniZSet<>();
    }

    @Override
    public boolean isBeyondLimit() {
        synchronized (miniZSet) {
            long timestamp = System.currentTimeMillis();
            //删除规定时间段之前的所有节点
            miniZSet.rangeRemove(0, timestamp - Status.LIMIT_TIME);
            if (miniZSet.getNodeCount(0) >= Status.LIMIT_NUMBER) {
                return true;
            }
            miniZSet.insert(timestamp, Status.OBJECT);
            return false;
        }
    }
}
