package com.rpc.registry.zookeeper.exception;

public class ZookeeperConnectionException extends RuntimeException{

    public ZookeeperConnectionException() {
        super();
    }


    public ZookeeperConnectionException(String message) {
        super(message);
    }


    public ZookeeperConnectionException(String message, Throwable cause) {
        super(message, cause);
    }


    public ZookeeperConnectionException(Throwable cause) {
        super(cause);
    }
}
