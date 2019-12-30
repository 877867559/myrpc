package com.rpc.core;

import com.rpc.core.model.ResultWrapper;

public class Response {

    //服务调用结果
    private ResultWrapper result;

    public ResultWrapper getResult() {
        return result;
    }

    public void setResult(ResultWrapper result) {
        this.result = result;
    }
}
