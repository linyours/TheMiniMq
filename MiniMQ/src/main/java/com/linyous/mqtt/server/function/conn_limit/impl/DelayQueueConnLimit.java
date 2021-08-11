package com.linyous.mqtt.server.function.conn_limit.impl;

import com.linyous.mqtt.server.common.Status;
import com.linyous.mqtt.server.function.conn_limit.ConnLimit;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Linyous
 * @date 2021/6/21 16:35
 */
public class DelayQueueConnLimit implements ConnLimit {

    private DelayQueue<Item> delayQueue;
    private AtomicInteger atomicInteger;

    public DelayQueueConnLimit() {
        this.delayQueue = new DelayQueue<>();
        this.atomicInteger = new AtomicInteger(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Item item = null;
                    try {
                        item = delayQueue.take();
                    } catch (InterruptedException e) {
                        continue;
                    }
                    if (item == null) continue;
                    if (atomicInteger.get() > 0) atomicInteger.getAndDecrement();
                }
            }
        }).start();
    }

    @Override
    public boolean isBeyondLimit() {
        if (atomicInteger.get() >= Status.LIMIT_NUMBER) return true;
        delayQueue.add(new Item(Status.LIMIT_TIME, TimeUnit.MILLISECONDS));
        atomicInteger.getAndIncrement();
        return false;
    }

    class Item implements Delayed {
        /* 触发时间*/
        private long time;

        public Item(long time, TimeUnit unit) {
            this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            Item item = (Item) o;
            long diff = this.time - item.time;
            if (diff <= 0) {// 改成>=会造成问题
                return -1;
            } else {
                return 1;
            }
        }
    }
}
