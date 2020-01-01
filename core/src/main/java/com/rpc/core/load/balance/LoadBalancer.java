package com.rpc.core.load.balance;

import java.util.List;

public interface LoadBalancer<T> {

    T select(List<T> list);
}
