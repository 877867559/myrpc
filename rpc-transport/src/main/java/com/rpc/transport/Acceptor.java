package com.rpc.transport;

import com.rpc.core.Server;

public interface Acceptor  extends Server {

    void start() throws InterruptedException;

    void shutdownGracefully();
}
