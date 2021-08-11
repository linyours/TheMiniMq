package com.linyous.mqtt.server.common;

import com.linyous.mqtt.server.entity.ClientInfo;
import com.linyous.mqtt.server.entity.Message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Linyous
 * @date 2021/6/18 17:17
 */
public class Cache {
    /**
     * 存储已经登录的客户端,String为客户端的ip和port
     */
    public final static ConcurrentHashMap<String, ClientInfo> CLIENT_MAP = new ConcurrentHashMap<String, ClientInfo>();
    /**
     * 存储ident与ip的映射关系
     */
    public final static ConcurrentHashMap<String, String> IDENT_IP_MAP = new ConcurrentHashMap<String, String>();
    /**
     * 连接锁
     */
    public final static Object CONN_LOCK = new Object();
    /**
     * 储存topic和订阅topic的客户端列表
     */
    public final static ConcurrentHashMap<String, List<String>> TOPIC_MAP = new ConcurrentHashMap<String, List<String>>();
    /**
     * 消息阻塞队列，存放为处理的消息
     */
    public final static LinkedBlockingQueue<Message> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
    /**
     * 记录正在处理QoS为1和2的消息
     */
    public final static ConcurrentHashMap<Integer, Object> MESSAGE_ID_CACHE = new ConcurrentHashMap<>();
    /**
     * 缓存QoS为2，没有收到Pubdel的消息
     */
    public final static ConcurrentHashMap<Integer, Message> MESSAGE_NOT_GET_PUB_DEL = new ConcurrentHashMap<>();
}
