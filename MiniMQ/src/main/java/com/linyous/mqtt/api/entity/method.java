package com.linyous.mqtt.api.entity;

import java.util.List;

/**
 * @author Linyous
 * @date 2021/6/24 11:05
 */
public class method {
    private String clazz;
    private String methodName;
    private List<Class> parameterType;
    private List<Object> parameters;

    public method() {
    }

    public method(String clazz, String methodName, List<Class> parameterType, List<Object> parameters) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.parameters = parameters;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Class> getParameterType() {
        return parameterType;
    }

    public void setParameterType(List<Class> parameterType) {
        this.parameterType = parameterType;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "method{" +
                "clazz='" + clazz + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterType=" + parameterType +
                ", parameters=" + parameters +
                '}';
    }
}
