package com.rpc.core.processor;

import com.rpc.core.Request;
import com.rpc.core.channel.Channel;

public interface ProviderProcessor {

    void handleRequest(Channel channel, Request request) throws Exception;
}
