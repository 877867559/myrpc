package com.rpc.core.channel;

import java.net.SocketAddress;

public interface Channel {

    /**
     * 返回Channel唯一标识
     * @return
     */
    String id();

    /**
     * Channel是否是活跃的
     * @return
     */
    boolean isActive();

    /**
     * 返回本地Socket地址
     * @return
     */
    SocketAddress localAddress();

    /**
     * 返回远程地址
     * @return
     */
    SocketAddress remoteAddress();

    /**
     * 发送消息
     * @param msg
     * @return
     */
    Channel write(Object msg);

    /**
     * 发送消息通过接口返回结果
     * @param msg
     * @param listener
     * @return
     */
    Channel write(Object msg, ChannelListener<Channel> listener);

    /**
     * 关闭Channel
     * @return
     */
    Channel close();
}
