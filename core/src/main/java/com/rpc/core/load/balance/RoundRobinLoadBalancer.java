package com.rpc.core.load.balance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer <T> implements LoadBalancer<T> {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public T select(List<T> list) {
        int length = list.size();
        if (length == 0) {
            throw new IllegalArgumentException("empty elements");
        }
        if (length == 1) {
            return (T) list.get(0);
        }
        return (T) list.get(atomicInteger.getAndIncrement() % length);
    }
}
