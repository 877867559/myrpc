package com.rpc.core.model;

import java.util.List;
import java.util.Map;

import static com.rpc.common.util.Preconditions.checkNotNull;

public class ServiceWrapper {

    private final ServiceMetadata metadata;     // 服务元信息
    private final Object serviceProvider;       // 服务对象
    // provider中所有接口的参数类型(methodName,List<Class<?>[]>)
    private transient Map<String, List<Class<?>[]>> methodsParameterTypes;


    public ServiceWrapper(String group, String version, String name, Object serviceProvider, Map<String, List<Class<?>[]>> methodsParameterTypes){
        metadata = new ServiceMetadata(group, version, name);
        this.serviceProvider = checkNotNull(serviceProvider, "serviceProvider");
        this.methodsParameterTypes = checkNotNull(methodsParameterTypes, "methodsParameterTypes");
    }

    public ServiceMetadata getMetadata() {
        return metadata;
    }

    public Object getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceWrapper wrapper = (ServiceWrapper) o;

        return metadata.equals(wrapper.metadata);
    }

    @Override
    public int hashCode() {
        return metadata.hashCode();
    }

    @Override
    public String toString() {
        return "ServiceWrapper{" +
                "metadata=" + metadata +
                ", serviceProvider=" + serviceProvider +
                '}';
    }
}
