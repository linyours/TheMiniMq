package com.linyous.mqtt.server.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Linyous
 * @date 2021/6/21 9:12
 */
public class Status {
    /**
     * 当前登录的连接数量
     */
    public static AtomicInteger CONN_COUNT = new AtomicInteger();
    /**
     * 最大连接数
     */
    public static int MAX_CONN_NUMS = 20;
    /**
     * 连接认证id
     */
    public static String AUTH_ID = "ac2ff8103f6fce1f";
    /**
     * 连接认证秘钥
     */
    public static String AUTH_SECRET = "6b34fe24ac2ff8103f6fce1f0da2ef57";
    /**
     * 充数的对象
     */
    public static Object OBJECT = new Object();
    /**
     * 最大的处理发布消息的子线程数量
     */
    public static int MAX_DEAL_PUB_MESSAGE_NUM = 5;
    /**
     * PUBLISH的消息ID
     */
    public static AtomicInteger MESSAGE_ID = new AtomicInteger(0);
    /**
     * 存储方式
     */
    public static String STORAGE_WAY = "log";
    /**
     * 速率限制方式
     */
    public static String LIMIT_WAY = "zset";
    /**
     * 限制时间段,毫秒
     */
    public static long LIMIT_TIME = 10000;
    /**
     * 限制连接数量
     */
    public static long LIMIT_NUMBER = 1000;
    /**
     * 黑名单方式
     */
    public static String BLACK_LIST_WAY = "bloomfilter";
}
