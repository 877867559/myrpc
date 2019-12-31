package com.rpc.core.load.balance;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer <T> implements LoadBalancer<T> {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public T select(Object[] elements) {
        int length = elements.length;
        if (length == 0) {
            throw new IllegalArgumentException("empty elements");
        }
        if (length == 1) {
            return (T) elements[0];
        }
        return (T) elements[atomicInteger.getAndIncrement() % length];
    }
}
