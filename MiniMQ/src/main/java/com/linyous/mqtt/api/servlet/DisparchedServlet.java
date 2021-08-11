package com.linyous.mqtt.api.servlet;

import com.alibaba.fastjson.JSONArray;
import com.linyous.mqtt.api.common.Cache;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Linyous
 * @date 2021/6/24 14:21
 */
public class DisparchedServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println(req.getRequestURI());
        String uri = req.getRequestURI().replace("/mini/", "");
        String[] param = uri.split("/");
        if (param.length < 2) {
            outputChineseByOutputStream(resp, "fail");
        } else {
            if (Cache.NAME_TO_CLASS.containsKey(param[0])) {
                Class clazz = Cache.NAME_TO_CLASS.get(param[0]);
                Object object = Cache.CLASS_TO_OBJECT.get(clazz);
                if (Cache.METHOD_PARAMETER.containsKey(param[0] + "." + param[1])) {
                    Method method = Cache.METHOD_PARAMETER.get(param[0] + "." + param[1]);
                    StringBuffer data = new StringBuffer();
                    BufferedReader reader = req.getReader();
                    String line = null;
                    try {
                        while (null != (line = reader.readLine())) data.append(line.trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        reader.close();
                    }
                    System.out.println(data.toString());
                    JSONArray jsonArray = JSONArray.parseArray(data.toString());
                    Object[] objects = jsonArray.toArray();
                    try {
                        method.invoke(object, objects);
                        outputChineseByOutputStream(resp, "success");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        outputChineseByOutputStream(resp, "fail");
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        outputChineseByOutputStream(resp, "fail");
                    }
                } else {
                    outputChineseByOutputStream(resp, "fail");
                }
            } else {
                outputChineseByOutputStream(resp, "fail");
            }
        }
    }

    /**
     * 使用OutputStream流输出中文
     *
     * @param response
     * @throws IOException
     */
    public void outputChineseByOutputStream(HttpServletResponse response, String data) throws IOException {
        /**使用OutputStream输出中文注意问题：
         * 在服务器端，数据是以哪个码表输出的，那么就要控制客户端浏览器以相应的码表打开，
         * 比如：outputStream.write("中国".getBytes("UTF-8"));//使用OutputStream流向客户端浏览器输出中文，以UTF-8的编码进行输出
         * 此时就要控制客户端浏览器以UTF-8的编码打开，否则显示的时候就会出现中文乱码，那么在服务器端如何控制客户端浏览器以以UTF-8的编码显示数据呢？
         * 可以通过设置响应头控制浏览器的行为，例如：
         * response.setHeader("content-type", "text/html;charset=UTF-8");//通过设置响应头控制浏览器以UTF-8的编码显示数据
         */
        OutputStream outputStream = response.getOutputStream();//获取OutputStream输出流
        response.setHeader("content-type", "text/html;charset=UTF-8");//通过设置响应头控制浏览器以UTF-8的编码显示数据，如果不加这句话，那么浏览器显示的将是乱码
        /**
         * data.getBytes()是一个将字符转换成字节数组的过程，这个过程中一定会去查码表，
         * 如果是中文的操作系统环境，默认就是查找查GB2312的码表，
         * 将字符转换成字节数组的过程就是将中文字符转换成GB2312的码表上对应的数字
         * 比如： "中"在GB2312的码表上对应的数字是98
         *         "国"在GB2312的码表上对应的数字是99
         */
        /**
         * getBytes()方法如果不带参数，那么就会根据操作系统的语言环境来选择转换码表，如果是中文操作系统，那么就使用GB2312的码表
         */
        byte[] dataByteArr = data.getBytes("UTF-8");//将字符转换成字节数组，指定以UTF-8编码进行转换
        outputStream.write(dataByteArr);//使用OutputStream流向客户端输出字节数组
    }
}
