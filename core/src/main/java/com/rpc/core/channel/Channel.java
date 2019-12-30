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
     * 关闭Channel
     * @return
     */
    Channel close();
}
