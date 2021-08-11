# TheTheMiniMQ

## 介绍

TheTheMiniMQ是基于Netty开发的物联网 MQTT 消息服务器。

## 软件架构

![软件架构](https://user-images.githubusercontent.com/71416178/128990088-addd3544-05db-4004-a679-3c04907d892d.png)
<br />
<br />
<br />
![软件架构](https://user-images.githubusercontent.com/71416178/128965964-bc822882-4973-4c65-9825-dd25ff36177f.png)

## 安装教程

可以用Maven形式去导入项目，入口类为com.linyous.mqtt.server.MQTTServer

## 内嵌Tomcat

TheMiniMQ通过内嵌tomcat对外提供HTTP接口供使用。

## 技术实现

### Mini SPI

#### 入口

```java
MiniExtensionLoader.getExtensionLoader(Class<T> type).getExtension(String name);
```

#### 概念解释

**JDK 的SPI** 全称为 (Service Provider Interface) ，是JDK内置的一种服务提供发现机制。 目前有不少框架用它来做服务的扩展发现， 简单来说，它就是一种动态替换发现的机制。

    不过JDK标准的SPI会一次性实例化扩展点所有实现，如果有扩展实现则初始化很耗时，如果没用上的也加载， 
    则会浪费资源

**DUBBO** 正是利用SPI机制实现服务拓展点的功能，与JDK SPI会一次性加载所有的扩展实现不同，DUBBO的SPI并不会加载所有的扩展实现，而只是加载所有拓展点的对象类，只在需要获取具体扩展点的时候才会利用反射去实例化扩展点，并进行缓存。在TheTheMiniMQ 中 Mini SPI在DUBBO SPI机制的基础上相对于DUBBO轻量化了许多功能（如AOP，IOC），只保留了TheMiniMQ需要用到的功能。



#### 流程图

流程图仅供参考，具体代码在spi包下，都有注释说明，可自行查看。
<br />
![流程图](https://user-images.githubusercontent.com/71416178/128969086-12b932a3-e1ae-4eb1-ab9a-0253180337d9.png)



#### 速率限制

TheMiniMQ中通过SPI机制，可选择固定窗口以及延时队列两种控制速率的算法，基于SPI机制，开发者可以自行定义自己的速率限制算法进行替代。

##### 固定窗口

固定窗口基于类Redis的ZSet进行实现，同样的底层也是基于跳表，TheMiniMQ实现了自己的跳表以及ZSet结构。
<br />
![跳表](https://user-images.githubusercontent.com/71416178/128994960-9f80499a-9a4c-42d5-bec9-dbbdabab13ba.png)

##### 延时队列

基于Java的DelayQueue实现。



#### 黑名单

TheMiniMQ实现了黑名单功能，可以控制客户端的连接，基于MINI SPI的功能，TheMiniMQ提供了两种黑名单的实现，布隆过滤器以及HashMap的实现，基于SPI机制，开发者可以自行定义自己的速率限制算法进行替代。

##### 布隆过滤器

关于布隆过滤器原理，通过多个hash算法，求出String的哈希值，并进行取余，余数为位数，比如int为4字节32位，所有有32个位置可以存储，将所有hash算法取余的之后对应的位置设置为1。查看时，同样用一样的hash算法计算，如果所有的bit都是1，证明该String在记录内。
<br />

##### HashMap

基于ConcurrentHashMap实现，这里不再赘述。

### PUBLISH QoS

#### 概念解释

1. QoS 0(At most once)“至多一次”
   客户端发送消息并不需要服务器的响应，无论是否成功，客户端都无所谓也不关心。
   
2. QoS 1(At least once)“至少一次”
   确保消息到达，但消息重复可能会发生。
   
3. QoS 2(Exactly once)“只有一次”
   确保消息到达一次。

#### 注意事项

1. 这里指的是Client和Broker之间的关系，而不是Publisher和Subscriber之间的关系，在Pub/Sub模型中，Publisher和Subscriber是完全解耦的。
2. 发布者在每次发布消息时都需要设置QoS，订阅者在订阅主题时也可以设置QoS。
3. 即使发布者设置发布消息为QoS2，订阅者也可以通过QoS0或QoS1来订阅该消息（这样就是QoS的降级downgrade）。

#### 时序图

![1622510577943](https://user-images.githubusercontent.com/71416178/128996724-6e9c34d3-766e-48b3-8e66-a6fc5d97c795.png)


<br />
![1622512670300](https://user-images.githubusercontent.com/71416178/128996740-aec8800b-a84b-4d19-8a4b-9d7043e26b34.png)

<br />
![1622512849148](https://user-images.githubusercontent.com/71416178/128996753-85ad4933-c2b9-408b-8583-2ff610ee13dd.png)
