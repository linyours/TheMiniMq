package com.linyous.mqtt.server.function.blacklist.impl;

import com.linyous.mqtt.server.function.blacklist.BlackList;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Linyous
 * @date 2021/6/21 17:49
 */
public class HashMapImpl implements BlackList {

    private Object object;

    private ConcurrentHashMap<String, Object> hashMap;

    public HashMapImpl() {
        this.object = new Object();
        this.hashMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isBlack(String id) {
        if (this.hashMap.containsKey(id)) return true;
        return false;
    }

    @Override
    public void addBlack(String id) {
        this.hashMap.put(id, this.object);
    }
}
