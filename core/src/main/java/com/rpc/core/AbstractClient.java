package com.rpc.core;

import com.rpc.core.channel.AddressChannelGroups;
import com.rpc.core.channel.Channel;
import com.rpc.core.channel.ChannelGroup;
import com.rpc.core.channel.MetadataChannelGroup;
import com.rpc.core.load.balance.LoadBalancer;
import com.rpc.core.load.balance.RoundRobinLoadBalancer;
import com.rpc.core.model.ServiceMetadata;
import com.rpc.core.model.UnresolvedAddress;
import com.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.rpc.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.rpc.common.util.Preconditions.checkNotNull;

public abstract class AbstractClient implements Client {

    private static Logger logger = LoggerFactory.getLogger(AbstractClient.class);
    //注册中心
    private  RegistryService registryService = new ZookeeperRegistryService();
    //一个接口对应的多个ip:port服务提供者
    private MetadataChannelGroup metadataChannelGroup = new MetadataChannelGroup();
    //一个ip:port建立了多个Channel（netty中的channel）
    private AddressChannelGroups addressChannelGroups = new AddressChannelGroups();
    //负载均衡接口
    private LoadBalancer<ChannelGroup> loadBalancer = new RoundRobinLoadBalancer<>();
    //客户端唯一标识
    private final String appName;

    public AbstractClient() {
        this("UNKNOWNAPPNAME");
    }

    public AbstractClient(String appName) {
        this.appName = appName;
    }

    @Override
    public void connectToRegistryServer(String connectString) {
        registryService.connectToRegistryServer(connectString);
    }

    @Override
    public String appName() {
        return appName;
    }

    @Override
    public Collection<RegisterMeta> lookup(ServiceMetadata metadata) {
        RegisterMeta.ServiceMeta serviceMeta = transformToServiceMeta(metadata);
        return registryService.lookup(serviceMeta);
    }

    @Override
    public void offlineListening(UnresolvedAddress address, OfflineListener listener) {
        if (registryService instanceof AbstractRegistryService) {
            ((AbstractRegistryService) registryService).offlineListening(transformToAddress(address), listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void subscribe(ServiceMetadata metadata, NotifyListener listener) {
        registryService.subscribe(transformToServiceMeta(metadata), listener);
    }
    private static RegisterMeta.Address transformToAddress(UnresolvedAddress address) {
        return new RegisterMeta.Address(address.getHost(), address.getPort());
    }
    private static RegisterMeta.ServiceMeta transformToServiceMeta(ServiceMetadata metadata) {
        RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta();
        serviceMeta.setGroup(checkNotNull(metadata.getGroup(), "group"));
        serviceMeta.setVersion(checkNotNull(metadata.getVersion(), "version"));
        serviceMeta.setServiceProviderName(checkNotNull(metadata.getServiceProviderName(), "serviceProviderName"));
        return serviceMeta;
    }
}
