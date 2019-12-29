package com.rpc.registry.zookeeper;

import org.rpc.registry.AbstractRegistryService;
import org.rpc.registry.RegisterMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperRegistryService extends AbstractRegistryService {

    private static Logger logger = LoggerFactory.getLogger((ZookeeperRegistryService.class));


    @Override
    public void connectToRegistryServer(String connectString) {

    }

    @Override
    protected  void doSubscribe(RegisterMeta.ServiceMeta serviceMeta){

    }
    @Override
    protected  void doRegister(RegisterMeta meta){

    }
    @Override
    protected  void doUnregister(RegisterMeta meta){

    }

    @Override
    public void destroy() {

    }

}