package com.rpc.core.channel;

public interface ChannelListener<C> {

    void operationSuccess(C c) throws Exception;

    void operationFailure(C c, Throwable throwable) throws Exception;
}
