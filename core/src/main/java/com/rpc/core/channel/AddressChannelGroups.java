package com.rpc.core.channel;

import com.rpc.common.util.Maps;
import com.rpc.core.model.UnresolvedAddress;

import java.util.concurrent.ConcurrentMap;

/**
 * 一个ip:port可能会建立多个连接也即多个Channel(这个管道是netty中的channel，不是自定义的)，但一般只共享一个
 */
public class AddressChannelGroups {

    private final ConcurrentMap<UnresolvedAddress, ChannelGroup> addressGroups = Maps.newConcurrentHashMap();

    public ChannelGroup find(UnresolvedAddress address) {
        return addressGroups.get(address);
    }

    public ChannelGroup add(UnresolvedAddress address,ChannelGroup group){
       return addressGroups.putIfAbsent(address,group);
    }
}
