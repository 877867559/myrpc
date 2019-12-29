package com.rpc.registry.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class ZookeeperTest {

    private ZooKeeper zkclient;

    private String connectionString = "127.0.0.1:2181";

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String groupNode = "/sgroup";

    private Stat stat = new Stat();

    //用Map存储服务器信息，key是服务器节点path，value是服务器信息。服务器信息又 包含了服务器名称和负载
    private volatile Map<String, ServerInfo> serverMap = new HashMap<>();


    @Before
    public void init() throws Exception {
        zkclient = new ZooKeeper(connectionString, 60 * 1000, new ZookeeperConnectionWatcher());
        countDownLatch.await();
    }

    @Test
    public void testZookeeperWatcherTest() throws Exception{
        zkclient.getData(groupNode,new ZookeeperWatcher(),stat);
        zkclient.getChildren(groupNode,new ZookeeperWatcher(),stat);
        try {
            zkclient.create(groupNode, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
          // e.printStackTrace();
        }
        zkclient.create(groupNode+"/1", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

    }

    @After
    public void close() throws InterruptedException {
        if(zkclient != null) zkclient.close();
    }


    /*
     * 更新服务器列表
     */
    private void updateServerList() throws Exception {
        Map<String, ServerInfo> newServerMap = new HashMap<String, ServerInfo>();

        // 获取并监听groupNode的子节点变化
        // watch参数为true, 表示监听子节点变化事件.
        // 每次都需要重新注册监听, 因为一次注册, 只能监听一次事件, 如果还想继续保持监听, 必须重新注册

        //这里的true表示注册监听，这里可以使用前面注册的默认Watcher，也可以自定义新的Watcher
        List<String> subList= zkclient.getChildren(groupNode,new ZookeeperWatcher(),stat);
        for(String subNode:subList){
            ServerInfo serverInfo=new ServerInfo();
            serverInfo.setPath(groupNode+"/"+subNode);
            serverInfo.setName(subNode);
            //获取每个子节点下关联的服务器负载的信息
            byte[] data = zkclient.getData(serverInfo.getPath(), false,stat);	//注册监听
            String loadBalance=new String(data,"utf-8");
            serverInfo.setLoadBalance(loadBalance);
            newServerMap.put(serverInfo.getPath(), serverInfo);

        }
        // 替换server列表
        serverMap = newServerMap;
        System.out.println("$$$更新了服务器列表:" + serverMap.toString());
    }

    /*
     * 更新服务器节点的负载数据
     */
    private void updataServerLoadBalance(String serverNodePath) throws Exception {
        ServerInfo serverInfo = serverMap.get(serverNodePath);
        if(null!=serverInfo){
            //获取每个子节点下关联的服务器负载的信息
            byte[] data= zkclient.getData(serverInfo.getPath(), true, stat);
            String loadBalance=new String(data,"utf-8");
            serverInfo.setLoadBalance(loadBalance);
            serverMap.put(serverInfo.getPath(), serverInfo);
            System.out.println("@@@更新了服务器的负载："+serverInfo);
            System.out.println("------");
            System.out.println("###更新服务器负载后，服务器列表信息："+serverMap);
        }
    }


    class ZookeeperConnectionWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("Enter the process method,the event is :" + event.toString());
            if(Event.KeeperState.SyncConnected == event.getState()){
                System.out.println("连接zk成功");
            }else{
                System.out.println("连接zk错误");
            }
            countDownLatch.countDown();
        }
    }

    class ZookeeperWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("Enter the process method,the event is :" + event.toString());

            //集群总节点的子节点变化触发的事件
            if (event.getType() == Event.EventType.NodeChildrenChanged && event.getPath().equals(groupNode)) {
                //如果发生了"/sgroup"节点下的子节点变化事件, 更新server列表, 并重新注册监听
                try {
                    updateServerList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (event.getType() == Event.EventType.NodeDataChanged && event.getPath().startsWith(groupNode)) {
                //如果发生了服务器节点数据变化事件, 更新server列表, 并重新注册监听
                try {
                    updataServerLoadBalance(event.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            if (event.getType() == Event.EventType.NodeChildrenChanged){
//                System.out.println("Children节点发生变化");
//            }else if(event.getType() == Event.EventType.NodeCreated){
//                System.out.println("节点被创建");
//            }else if(event.getType() == Event.EventType.NodeDataChanged){
//                System.out.println("节点发生变化");
//            }else if(event.getType() == Event.EventType.NodeDeleted){
//                System.out.println("节点被删除");
//            }else if(event.getType() == Event.EventType.None){
//                System.out.println("节点未发生变化");
//            }
        }
    }

    class ServerInfo{

        private String path;

        private String name;

        private String loadBalance;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLoadBalance() {
            return loadBalance;
        }

        public void setLoadBalance(String loadBalance) {
            this.loadBalance = loadBalance;
        }
    }
}
