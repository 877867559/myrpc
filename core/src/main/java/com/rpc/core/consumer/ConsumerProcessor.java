package com.rpc.core.consumer;

import com.rpc.core.Response;
import com.rpc.core.channel.Channel;

public interface ConsumerProcessor {

    void handleResponse(Channel channel, Response response) throws Exception;
}
