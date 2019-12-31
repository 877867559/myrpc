package com.rpc.core;

import com.rpc.core.channel.Channel;
import com.rpc.core.channel.ChannelGroup;
import com.rpc.core.model.ServiceMetadata;
import com.rpc.core.model.UnresolvedAddress;
import org.rpc.registry.NotifyListener;
import org.rpc.registry.OfflineListener;
import org.rpc.registry.RegisterMeta;
import org.rpc.registry.Registry;

import java.util.Collection;

public interface Client  extends Registry {

    /**
     * 返回一个应用名称
     * @return
     */
    String appName();

    /**
     * 根据ip:port返回ChannelGroup
     * @param address
     * @return
     */
    ChannelGroup group(UnresolvedAddress address);


    /**
     * 添加group
     * @param metadata
     * @param group
     * @return
     */
    boolean addChannelGroup(ServiceMetadata metadata, ChannelGroup group);

    /**
     * 移除group
     * @param metadata
     * @param group
     * @return
     */
    boolean removeChannelGroup(ServiceMetadata metadata, ChannelGroup group);

    /**
     * 查找ChannelGroup
     * @param metadata
     * @return
     */
    Collection<ChannelGroup> metadata(ServiceMetadata metadata);

    /**
     * metadata是否有可用的服务
     * @param metadata
     * @return
     */
    boolean isDirectoryAvailable(ServiceMetadata metadata);

    /**
     * 根据服务名称选择一个Channel
     * @param metadata
     * @return
     */
    Channel select(ServiceMetadata metadata);

    /**
     * 本地查找服务
     * @param metadata
     * @return
     */
    Collection<RegisterMeta> lookup(ServiceMetadata metadata);


    /**
     * 订阅服务
     * @param metadata
     * @param listener
     */
    void subscribe(ServiceMetadata metadata, NotifyListener listener);

    /**
     * 下线服务
     * @param address
     * @param listener
     */
    void offlineListening(UnresolvedAddress address, OfflineListener listener);
}
