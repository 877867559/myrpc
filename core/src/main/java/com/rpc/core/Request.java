package com.rpc.core;

import com.rpc.core.model.ClientWrapper;

public class Request {

    //请求数据
    private ClientWrapper clientWrapper;


    public ClientWrapper getClientWrapper() {
        return clientWrapper;
    }

    public void setClientWrapper(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }
}
