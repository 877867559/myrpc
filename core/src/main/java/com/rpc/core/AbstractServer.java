package com.rpc.core;

import com.rpc.common.util.Maps;
import com.rpc.core.model.ServiceWrapper;
import com.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.rpc.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

public abstract  class AbstractServer implements Server{

    private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    //注册中心
    private final RegistryService registryService = new ZookeeperRegistryService();
    //本地注册服务
    private final ConcurrentMap<String, ServiceWrapper> serviceProviders = Maps.newConcurrentHashMap();
    //服务端唯一标识
    private  String appName;
}
