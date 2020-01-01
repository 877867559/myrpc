package com.rpc.core;

import com.rpc.common.util.Lists;
import com.rpc.common.util.Maps;
import com.rpc.common.util.StringUtils;
import com.rpc.core.model.ServiceMetadata;
import com.rpc.core.model.ServiceWrapper;
import com.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.rpc.registry.RegisterMeta;
import org.rpc.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import static com.rpc.common.util.Preconditions.checkArgument;
import static com.rpc.common.util.Preconditions.checkNotNull;

public abstract  class AbstractServer implements Server{

    private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    //注册中心
    private final RegistryService registryService = new ZookeeperRegistryService();
    //本地注册服务
    private final ConcurrentMap<String, ServiceWrapper> serviceProviders = Maps.newConcurrentHashMap();

    @Override
    public void connectToRegistryServer(String connectString) {
        registryService.connectToRegistryServer(connectString);
    }

    /**
     * 本地查找服务
     * @param serviceMetadata
     * @return
     */
    @Override
    public ServiceWrapper lookupService(ServiceMetadata serviceMetadata) {
        return serviceProviders.get(serviceMetadata.metadata());
    }

    @Override
    public void publish(ServiceWrapper serviceWrapper) {
        ServiceMetadata metadata = serviceWrapper.getMetadata();

        RegisterMeta meta = new RegisterMeta();
        meta.setPort(bindPort());
        meta.setGroup(metadata.getGroup());
        meta.setVersion(metadata.getVersion());
        meta.setServiceProviderName(metadata.getServiceProviderName());

        registryService.register(meta);
    }

    abstract int bindPort();

    @Override
    public void unpublish(ServiceWrapper serviceWrapper) {
        ServiceMetadata metadata = serviceWrapper.getMetadata();

        RegisterMeta meta = new RegisterMeta();
        meta.setPort(bindPort());
        meta.setGroup(metadata.getGroup());
        meta.setVersion(metadata.getVersion());
        meta.setServiceProviderName(metadata.getServiceProviderName());

        registryService.unregister(meta);
    }

    @Override
    public ServiceRegistry serviceRegistry() {
        return new DefaultServiceRegistry();
    }

    ServiceWrapper registerService(
            String group,
            String version,
            String providerName,
            Object serviceProvider,
            Map<String, List<Class<?>[]>> methodsParameterTypes) {
        ServiceWrapper wrapper = new ServiceWrapper(group, version, providerName, serviceProvider, methodsParameterTypes);
        serviceProviders.put(wrapper.getMetadata().metadata(), wrapper);
        return wrapper;
    }
    class DefaultServiceRegistry implements ServiceRegistry {

        private Object serviceProvider;                     // 服务对象
        private int weight;                                 // 权重
        private Executor executor;                        // 该服务私有的线程池

        @Override
        public ServiceRegistry provider(Object serviceProvider) {
            this.serviceProvider = serviceProvider;
            return this;
        }

        @Override
        public ServiceRegistry weight(int weight) {
            this.weight = weight;
            return this;
        }

        @Override
        public ServiceRegistry executor(Executor executor) {
            this.executor = executor;
            return this;
        }
        @Override
        public ServiceWrapper register() {
            ServiceProvider annotation = null;
            String providerName = null;
            Map<String, List<Class<?>[]>> methodsParameterTypes = Maps.newHashMap();
            for (Class<?> cls = serviceProvider.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
                Class<?>[] interfaces = cls.getInterfaces();
                if (interfaces != null) {
                    for (Class<?> providerInterface : interfaces) {
                        annotation = providerInterface.getAnnotation(ServiceProvider.class);
                        if (annotation == null) {
                            continue;
                        }
                        providerName = annotation.value();
                        providerName = StringUtils.isNotBlank(providerName) ? providerName : providerInterface.getSimpleName();
                        for (Method method : providerInterface.getMethods()) {
                            String methodName = method.getName();
                            List<Class<?>[]> list = methodsParameterTypes.get(methodName);
                            if (list == null) {
                                list = Lists.newArrayList();
                                methodsParameterTypes.put(methodName, list);
                            }
                            list.add(method.getParameterTypes());
                        }
                        break;
                    }
                }
                if (annotation != null) {
                    break;
                }
            }

            checkArgument(annotation != null, serviceProvider.getClass() + " is not a ServiceProvider");

            String group = annotation.group();
            String version = annotation.version();

            checkNotNull(group, "group");

            return registerService(
                    group,
                    version,
                    providerName,
                    serviceProvider,
                    methodsParameterTypes
            );
        }
    }
}
