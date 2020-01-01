package com.rpc.transport;

import com.rpc.core.Client;
import com.rpc.core.model.UnresolvedAddress;

public interface Connector extends Client {

    void connect(UnresolvedAddress address) throws InterruptedException;

    void shutdownGracefully();
}
