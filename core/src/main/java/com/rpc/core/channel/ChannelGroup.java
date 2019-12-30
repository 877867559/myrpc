package com.rpc.core.channel;

import com.rpc.core.model.UnresolvedAddress;

import java.util.List;

/**
 * 为什么用group,因为多个服务会提供同一个接口,接口名称:(ip:port)一比多的关系
 */
public interface ChannelGroup {

    /**
     * 返回远程地址
     * @return
     */
    UnresolvedAddress remoteAddress();

    /**
     * 选择一个Channel
     * @return
     */
    Channel next();

    /**
     * 返回所有的Channel
     * @return
     */
    List<? extends Channel> channels();

    /**
     * Channel是否为空
     * @return
     */
    boolean isEmpty();

    /**
     * 添加Channel
     * @param channel
     * @return
     */
    boolean add(Channel channel);

    /**
     * 移除Channel
     * @param channel
     * @return
     */
    boolean remove(Channel channel);

    /**
     * 返回Channel数量
     * @return
     */
    int size();
}
