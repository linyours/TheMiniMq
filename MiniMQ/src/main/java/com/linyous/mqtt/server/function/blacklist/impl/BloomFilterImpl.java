package com.linyous.mqtt.server.function.blacklist.impl;

import com.linyous.mqtt.server.function.blacklist.BlackList;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;

/**
 * @author Linyous
 * @date 2021/6/21 17:42
 */
public class BloomFilterImpl implements BlackList {

    BloomFilter<String> bloomFilter;

    public BloomFilterImpl() {
        this.bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 100000, 0.001);
    }

    @Override
    public boolean isBlack(String id) {
        if (this.bloomFilter.mightContain(id)) return true;
        return false;
    }

    @Override
    public void addBlack(String id) {
        this.bloomFilter.put(id);
    }
}
