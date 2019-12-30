package com.rpc.core.model;

import java.util.Arrays;

public class ClientWrapper {

    private String appName;                 // 应用名称
    private final ServiceMetadata metadata; // metadata
    private String methodName;              // 方法名称
    private Object[] args;                  // 目标方法参数

    public ClientWrapper(ServiceMetadata metadata) {
        this.metadata = metadata;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ServiceMetadata getMetadata() {
        return metadata;
    }

    public String getGroup() {
        return metadata.getGroup();
    }

    public String getVersion() {
        return metadata.getVersion();
    }

    public String getServiceProviderName() {
        return metadata.getServiceProviderName();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }


    @Override
    public String toString() {
        return "MessageWrapper{" +
                "appName='" + appName + '\'' +
                ", metadata=" + metadata +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
