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
     * ??????OutputStream???????????????
     *
     * @param response
     * @throws IOException
     */
    public void outputChineseByOutputStream(HttpServletResponse response, String data) throws IOException {
        /**??????OutputStream???????????????????????????
         * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         * ?????????outputStream.write("??????".getBytes("UTF-8"));//??????OutputStream??????????????????????????????????????????UTF-8?????????????????????
         * ???????????????????????????????????????UTF-8???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????UTF-8???????????????????????????
         * ???????????????????????????????????????????????????????????????
         * response.setHeader("content-type", "text/html;charset=UTF-8");//???????????????????????????????????????UTF-8?????????????????????
         */
        OutputStream outputStream = response.getOutputStream();//??????OutputStream?????????
        response.setHeader("content-type", "text/html;charset=UTF-8");//???????????????????????????????????????UTF-8????????????????????????????????????????????????????????????????????????????????????
        /**
         * data.getBytes()??????????????????????????????????????????????????????????????????????????????????????????
         * ????????????????????????????????????????????????????????????GB2312????????????
         * ?????????????????????????????????????????????????????????????????????GB2312???????????????????????????
         * ????????? "???"???GB2312??????????????????????????????98
         *         "???"???GB2312??????????????????????????????99
         */
        /**
         * getBytes()?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????GB2312?????????
         */
        byte[] dataByteArr = data.getBytes("UTF-8");//??????????????????????????????????????????UTF-8??????????????????
        outputStream.write(dataByteArr);//??????OutputStream?????????????????????????????????
    }
}
