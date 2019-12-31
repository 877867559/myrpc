package com.rpc.core.load.balance;

public interface LoadBalancer<T> {

    T select(Object[] elements);
}
