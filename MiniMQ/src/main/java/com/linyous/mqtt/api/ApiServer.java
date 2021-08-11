package com.linyous.mqtt.api;

import com.linyous.mqtt.api.annotation.APIClass;
import com.linyous.mqtt.api.common.Cache;
import com.linyous.mqtt.api.common.Status;
import com.linyous.mqtt.api.client.NettyClient;
import com.linyous.mqtt.util.ClassUtil;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Linyous
 * @date 2021/6/24 11:01
 */
public class ApiServer {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyClient.run();
            }
        }).start();
        analyClass("com.chris.mqtt.api.service");
        //构建tomcat对象,此对象为启动tomcat服务的入口对象
        Tomcat t = new Tomcat();
        //构建Connector对象,此对象负责与客户端的连接.
        Connector con = new Connector("HTTP/1.1");
        //设置服务端的监听端口
        con.setPort(Status.API_PORT);
        //将Connector注册到service中
        t.getService().addConnector(con);
        //注册servlet
        Context ctx = t.addContext("/", null);
        Tomcat.addServlet(ctx, "DisparchedServlet", "com.chris.mqtt.api.servlet.DisparchedServlet");
        //映射servlet
        ctx.addServletMappingDecoded("/" + Status.APPLICATION_NAME + "/*", "DisparchedServlet");
        try {
            //启动tomcat
            t.start();
            //阻塞当前线程
            t.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    private static void analyClass(String basePackage) {
        Set<Class<?>> classes = null;
        try {
            classes = ClassUtil.getClasses(basePackage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (classes == null || classes.size() == 0) return;
        for (Class<?> aClass : classes) {
            //获得接口上的MiniSPI
            final APIClass defaultAnnotation = aClass.getAnnotation(APIClass.class);
            if (defaultAnnotation != null) {
                // 拿到MiniSPI注解中的Value，该value可以为默认的扩展点
                String value = defaultAnnotation.value();
                if ((value = value.trim()).length() > 0) {
                    try {
                        Cache.CLASS_TO_OBJECT.put(aClass, aClass.newInstance());
                        Cache.NAME_TO_CLASS.put(value, aClass);
                        for (Method declaredMethod : aClass.getDeclaredMethods()) {
                            Cache.METHOD_PARAMETER.put(value + "." + declaredMethod.getName(), declaredMethod);
                        }
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Cache.CLASS_TO_OBJECT.put(aClass, aClass.newInstance());
                        Cache.NAME_TO_CLASS.put(aClass.getSimpleName(), aClass);
                        for (Method declaredMethod : aClass.getDeclaredMethods()) {
                            Cache.METHOD_PARAMETER.put(aClass.getSimpleName() + "." + declaredMethod.getName(), declaredMethod);
                        }
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
