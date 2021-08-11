# TheMiniMQ

#### 介绍
MiniMQ是基于Netty开发物联网 MQTT 消息服务器。

#### 软件架构
![image](https://user-images.githubusercontent.com/71416178/128964534-db1ce691-fd90-432f-84a9-b498308abd8a.png)
<br />
![image](https://user-images.githubusercontent.com/71416178/128965964-bc822882-4973-4c65-9825-dd25ff36177f.png)

#### 安装教程
以Maven形式导入项目，入口类为com.linyous.mqtt.server.MQTTServer。
#### 内嵌Tomcat
MiniMQ通过内嵌tomcat对外提供HTTP接口供使用。
#### 技术实现
##### PUBLISH QoS
###### 概念解释
1. QoS 0(At most once)“至多一次”
客户端发送消息并不需要服务器的响应，无论是否成功，客户端都无所谓也不关心。
2. QoS 1(At least once)“至少一次”
确保消息到达，但消息重复可能会发生。
3. QoS 2(Exactly once)“只有一次”
确保消息到达一次。
###### 注意事项
1. 这里指的是Client和Broker之间的关系，而不是Publisher和Subscriber之间的关系，在Pub/Sub模型中，Publisher和Subscriber是完全解耦的。
2. 发布者在每次发布消息时都需要设置QoS，订阅者在订阅主题时也可以设置QoS。
3. 即使发布者设置发布消息为QoS2，订阅者也可以通过QoS0或QoS1来订阅该消息（这样就是QoS的降级downgrade）。
###### 时序图
![QoS = 0](https://images.gitee.com/uploads/images/2021/0520/113306_7e1ef4df_9138780.png "屏幕截图.png")
<br />
![QoS = 1](https://images.gitee.com/uploads/images/2021/0520/113344_a7752e50_9138780.png "屏幕截图.png")
<br />
![QoS = 2](https://images.gitee.com/uploads/images/2021/0520/113349_abe5938d_9138780.png "屏幕截图.png")
##### Mini SPI
###### 入口
```java
MiniExtensionLoader.getExtensionLoader(Class<T> type).getExtension(String name);
```
###### 概念解释
SPI 全称为 (Service Provider Interface) ，是JDK内置的一种服务提供发现机制。 目前有不少框架用它来做服务的扩展发现， 简单来说，它就是一种动态替换发现的机制。DUBBO 正是利用SPI机制实现服务拓展点的功能，与JDK SPI会一次性加载所有的扩展实现不同，DUBBO的SPI并不会加载所有的扩展实现，而只是加载所有拓展点的对象类，只在需要获取具体扩展点的时候才会利用反射去实例化扩展点，并进行缓存。Mini SPI在DUBBO SPI机制的基础上轻量化了许多功能（如AOP，IOC），只保留了MiniMQ需要用到的功能。
###### 流程图
流程图仅供参考，具体代码在spi包下，都有注释说明，可自行查看。
<br />
![输入图片说明](https://images.gitee.com/uploads/images/2021/0521/150444_c5efc614_9138780.png "屏幕截图.png")
##### 速率限制
MiniMQ中通过SPI机制，可选择固定窗口以及延时队列两种控制速率的算法，基于SPI机制，开发者可以自行定义自己的速率限制算法进行替代。
###### 固定窗口
固定窗口基于类Redis的ZSet进行实现，同样的底层也是基于跳表，MiniMQ实现了自己的跳表以及ZSet结构。
<br />
![输入图片说明](https://images.gitee.com/uploads/images/2021/0521/171156_1a4935fe_9138780.png "屏幕截图.png")
###### 延时队列
基于Java的DelayQueue实现。
##### 黑名单
MiniMQ实现了黑名单功能，可以控制客户端的连接，基于MINI SPI的功能，MiniMQ提供了两种黑名单的实现，布隆过滤器以及HashMap的实现，基于SPI机制，开发者可以自行定义自己的速率限制算法进行替代。
###### 布隆过滤器
关于布隆过滤器原理，通过多个hash算法，求出String的哈希值，并进行取余，余数为位数，比如int为4字节32位，所有有32个位置可以存储，将所有hash算法取余的之后对应的位置设置为1。查看时，同样用一样的hash算法计算，如果所有的bit都是1，证明该String在记录内。
<br />
![输入图片说明](https://images.gitee.com/uploads/images/2021/0521/181033_513597e8_9138780.png "屏幕截图.png")
###### HashMap
基于ConcurrentHashMap实现，这里不再赘述。
