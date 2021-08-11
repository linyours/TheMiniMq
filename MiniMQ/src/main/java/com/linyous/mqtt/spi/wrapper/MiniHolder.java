package com.linyous.mqtt.spi.wrapper;

/**
 * @author Linyous
 * @date 2021/6/21 11:23
 */
public class MiniHolder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
