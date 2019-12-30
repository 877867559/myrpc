package org.rpc.registry;

import java.util.Collection;


public interface RegistryService extends Registry {


    void register(RegisterMeta meta);


    void unregister(RegisterMeta meta);


    void subscribe(RegisterMeta.ServiceMeta serviceMeta, NotifyListener listener);


    Collection<RegisterMeta> lookup(RegisterMeta.ServiceMeta serviceMeta);
}
