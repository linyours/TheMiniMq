package com.linyous.mqtt.api.common;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Linyous
 * @date 2021/6/24 11:02
 */
public class Cache {
    /**
     * 储存className和class类对象的映射
     */
    public static final ConcurrentHashMap<String, Class> NAME_TO_CLASS = new ConcurrentHashMap<>();
    /**
     * 储存Class和实例对象的映射
     */
    public static final ConcurrentHashMap<Class, Object> CLASS_TO_OBJECT = new ConcurrentHashMap<>();
    /**
     * 储存方法和参数的映射
     */
    public static final ConcurrentHashMap<String, Method> METHOD_PARAMETER = new ConcurrentHashMap<>();
    /**
     * 缓存cannel
     */
    public static ChannelHandlerContext channelHandlerContext = null;
}
