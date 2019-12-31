package com.rpc.core;

import com.rpc.core.model.ServiceMetadata;
import com.rpc.core.model.ServiceWrapper;
import org.rpc.registry.Registry;

import java.util.concurrent.Executor;

public interface Server extends Registry {


    /**
     * Service注册.
     */
    interface ServiceRegistry {

        /**
         * 设置service注册提供者
         * @param serviceProvider
         * @return
         */
        ServiceRegistry provider(Object serviceProvider);

        /**
         * 设置service权重
         * @param weight
         * @return
         */
        ServiceRegistry weight(int weight);


        /**
         * 设置线程
         * @param executor
         * @return
         */
        ServiceRegistry executor(Executor executor);

        /**
         * 提供注册方法
         * @return
         */
        ServiceWrapper register();
    }

    /**
     * 返回ServiceRegistry实现类
     * @return
     */
    ServiceRegistry serviceRegistry();

    /**
     * 本地容器查找服务
     * @param serviceMetadata
     * @return
     */
    ServiceWrapper lookupService(ServiceMetadata serviceMetadata);
    /**
     * 发布一个service
     * @return
     */
    void publish(ServiceWrapper serviceWrapper);

    /**
     * 取消发布服务。
     * @param serviceWrapper
     */
    void unpublish(ServiceWrapper serviceWrapper);
}
