package com.rpc.registry.zookeeper;

import com.rpc.common.util.Lists;
import com.rpc.common.util.Maps;
import com.rpc.common.util.NetUtil;
import com.rpc.common.util.SystemPropertyUtil;
import com.rpc.registry.zookeeper.exception.ZookeeperConnectionException;
import org.apache.zookeeper.*;
import org.rpc.registry.AbstractRegistryService;
import org.rpc.registry.RegisterMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.rpc.common.util.Preconditions.checkNotNull;
import static org.rpc.registry.NotifyListener.NotifyEvent.*;

public class ZookeeperRegistryService extends AbstractRegistryService {

    private static Logger logger = LoggerFactory.getLogger((ZookeeperRegistryService.class));

    private ZooKeeper zkClient;

    private final int sessionTimeoutMs = SystemPropertyUtil.getInt("rpc.registry.zookeeper.sessionTimeoutMs", 60 * 1000);

    //service提供的ip注册到zk，供client调用
    private final String address = SystemPropertyUtil.get("rpc.address", NetUtil.getLocalAddress());

    // 指定节点都提供了哪些服务
    private final ConcurrentMap<RegisterMeta.Address, ConcurrentMap<RegisterMeta.ServiceMeta,Boolean>> serviceMetaMap = Maps.newConcurrentHashMap();

    private final ConcurrentMap<RegisterMeta.ServiceMeta, Watcher> pathChildrenCaches = Maps.newConcurrentHashMap();

    private final String zkGruopPath = "/rpc/provider";

    /**
     * 连接注册中心
     * @param connectString list of servers to connect to [host1:port1,host2:port2....]
     */
    @Override
    public void connectToRegistryServer(String connectString) {
        checkNotNull(connectString, "connectString");
        setConnectString(connectString);
        try{
            zkClient = new ZooKeeper(connectString, sessionTimeoutMs, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if(Event.KeeperState.SyncConnected == event.getState()){
                        if(isConnectRegistry()){
                            // 重新订阅
                            for(ConcurrentMap.Entry<RegisterMeta.ServiceMeta, Boolean> entry : getSubscribeMap().entrySet()){
                                doSubscribe(entry.getKey());
                            }
                            // 重新发布服务
                            for (ConcurrentMap.Entry<RegisterMeta, Boolean> entry: getRegisterMetaMap().entrySet()) {
                                doRegister(entry.getKey());
                            }
                        }else{
                            setConnectRegistry(true);
                        }
                    }else{
                        throw new ZookeeperConnectionException("连接zk失败:"+connectString);
                    }
                }
            });
        }catch (Exception e){
            throw new ZookeeperConnectionException("连接zk异常:"+e);
        }
    }
    @Override
    public Collection<RegisterMeta> lookup(RegisterMeta.ServiceMeta serviceMeta) {
        String directory = String.format("%s/%s/%s/%s",
                zkGruopPath,
                serviceMeta.getGroup(),
                serviceMeta.getVersion(),
                serviceMeta.getServiceProviderName());
        List<RegisterMeta> registerMetaList = Lists.newArrayList();
        try {
            List<String> paths = zkClient.getChildren(directory,false);
            for (String p : paths) {
                registerMetaList.add(parseRegisterMeta(String.format("%s/%s", directory, p)));
            }
        }catch (Exception e){
            logger.warn("lookup service meta: {} path failed, {}.", serviceMeta, e);
        }
        return registerMetaList;
    }
    @Override
    protected  void doSubscribe(RegisterMeta.ServiceMeta serviceMeta){
        Watcher watcher = pathChildrenCaches.get(serviceMeta);
        if (watcher == null) {
            String directory = String.format("%s/%s/%s/%s",
                    zkGruopPath,
                    serviceMeta.getGroup(),
                    serviceMeta.getVersion(),
                    serviceMeta.getServiceProviderName());
            Watcher newWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    logger.info("child event: {}", event);
                    switch(event.getState()) {
                        case SyncConnected:
                            switch (event.getType()) {
                                case NodeCreated: {
                                    RegisterMeta registerMeta = parseRegisterMeta(event.getPath());
                                    RegisterMeta.Address address = registerMeta.getAddress();
                                    RegisterMeta.ServiceMeta serviceMeta = registerMeta.getServiceMeta();
                                    ConcurrentMap<RegisterMeta.ServiceMeta,Boolean> serviceMetaSet = getServiceMeta(address);
                                    serviceMetaSet.put(serviceMeta,true);
                                    //版本号暂时写成1
                                    ZookeeperRegistryService.this.notify(serviceMeta, registerMeta, CHILD_ADDED, 1);

                                    break;
                                }
                                case NodeDeleted: {
                                    RegisterMeta registerMeta = parseRegisterMeta(event.getPath());
                                    RegisterMeta.Address address = registerMeta.getAddress();
                                    RegisterMeta.ServiceMeta serviceMeta = registerMeta.getServiceMeta();
                                    ConcurrentMap<RegisterMeta.ServiceMeta,Boolean> serviceMetaSet = getServiceMeta(address);
                                    serviceMetaSet.remove(serviceMeta);
                                    //版本号暂时写成1
                                    ZookeeperRegistryService.this.notify(serviceMeta, registerMeta, CHILD_REMOVED, 1);
                                    if (serviceMetaSet.isEmpty()) {
                                        logger.info("offline notify: {}.", address);
                                        ZookeeperRegistryService.this.offline(address);
                                    }
                                    break;
                                }
                            }
                            break;
                        case Expired:
                            // session过期,这是个非常严重的问题,有可能client端出现了问题,也有可能zk环境故障
                            logger.warn("zk session expired,zk address:{}",getConnectString());
                            connectToRegistryServer(getConnectString());
                            break;
                        case Disconnected:
                            logger.info("zk disconnected,zk address:{}",getConnectString());
                            connectToRegistryServer(getConnectString());
                            break;
                        case AuthFailed:
                            destroy();
                            throw new RuntimeException("ZK Connection auth failed,zk address:"+getConnectString());
                        default:
                            break;
                    }
                    addZKWatcher(directory);
                }
            };
            watcher = pathChildrenCaches.putIfAbsent(serviceMeta,newWatcher);
            if(watcher == null){
                watcher = newWatcher;
                zkClient.register(watcher);
                addZKWatcher(directory);
            }
        }
    }
    private void addZKWatcher(String directory){
        try{
            zkClient.getData(directory, true, null);
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }
    @Override
    protected  void doRegister(RegisterMeta meta){
        String directory = String.format("%s/%s/%s/%s",
                zkGruopPath,
                meta.getGroup(),
                meta.getVersion(),
                meta.getServiceProviderName());

        meta.setHost(address);

        directory = String.format("%s/%s:%s:%s:%s",
                directory,
                meta.getHost(),
                String.valueOf(meta.getPort()),
                String.valueOf(meta.getWeight()),
                String.valueOf(meta.getConnCount()));
        try {
            zkClient.create(directory, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (Exception e) {
            logger.warn("create path failed, directory: {}, {}.", directory, e);
        }
    }
    @Override
    protected  void doUnregister(RegisterMeta meta){
        String directory = String.format("%s/%s/%s/%s",
                zkGruopPath,
                meta.getGroup(),
                meta.getVersion(),
                meta.getServiceProviderName());

        meta.setHost(address);

        directory = String.format("%s/%s:%s:%s:%s",
                directory,
                meta.getHost(),
                String.valueOf(meta.getPort()),
                String.valueOf(meta.getWeight()),
                String.valueOf(meta.getConnCount()));
        try {
            zkClient.delete(directory, -1);
        } catch (Exception e) {
            logger.warn("delete path failed, directory: {}, {}.", directory, e);
        }
    }
    private RegisterMeta parseRegisterMeta(String data) {
        String[] array_0 = data.split(data, '/');
        RegisterMeta meta = new RegisterMeta();
        meta.setGroup(array_0[2]);
        meta.setVersion(array_0[3]);
        meta.setServiceProviderName(array_0[4]);

        String[] array_1 = array_0[5].split(array_0[5], ':');
        meta.setHost(array_1[0]);
        meta.setPort(Integer.parseInt(array_1[1]));
        meta.setWeight(Integer.parseInt(array_1[2]));
        meta.setConnCount(Integer.parseInt(array_1[3]));

        return meta;
    }

    private ConcurrentMap<RegisterMeta.ServiceMeta,Boolean> getServiceMeta(RegisterMeta.Address address) {
        ConcurrentMap<RegisterMeta.ServiceMeta,Boolean> serviceMetaSet = serviceMetaMap.get(address);
        if (serviceMetaSet == null) {
            ConcurrentMap<RegisterMeta.ServiceMeta,Boolean> newServiceMetaSet = Maps.newConcurrentHashMap();
            serviceMetaSet = serviceMetaMap.putIfAbsent(address, newServiceMetaSet);
            if (serviceMetaSet == null) {
                serviceMetaSet = newServiceMetaSet;
            }
        }
        return serviceMetaSet;
    }
    @Override
    public void destroy() {
        try {
            if(zkClient != null) zkClient.close();
        } catch (Exception e) {
            logger.warn("zk close exception:{}",e);
        }
    }
}