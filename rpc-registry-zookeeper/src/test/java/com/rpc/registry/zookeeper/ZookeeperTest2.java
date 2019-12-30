package com.rpc.registry.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class ZookeeperTest2 {

    ZooKeeper zkClient = null;

    String createNodeName = "/apiTest10000000007"; //记录创建的节点名称

    private Stat stat = new Stat();

    /**
     * 建立连接
     * @throws IOException
     */
    @Before
    public void connect() throws IOException {
        if(zkClient == null){
            zkClient = new ZooKeeper("127.0.0.1:2181", 1000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println("success to connect zk cluster!");
                }
            });
        }
    }

    /**
     * 测试创建节点
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testCreate() throws KeeperException, InterruptedException {
        createNodeName = zkClient.create(createNodeName, "this is api test1 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("创建节点的返回值是："+createNodeName);
    }

    /**
     * ls命令
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testLs() throws KeeperException, InterruptedException {
        List<String> children = zkClient.getChildren("/", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("获得监听事件,path:" + watchedEvent.getPath() + ";state" + watchedEvent.getState() + ";type:" + watchedEvent.getType());
            }
        });

        for (int i = 0; i < children.size(); i++) {
            System.out.println("ls / 数据："+children.get(i));
        }
    }

    /**
     * set命令
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testSet() throws KeeperException, InterruptedException {
        Stat stat = zkClient.setData(createNodeName, ("this is update data! "+System.currentTimeMillis()).getBytes(), -1);//-1表示让系统维护version
        System.out.println("set 返回数据：:"+stat);
    }

    @Test
    public void testCh() throws KeeperException, InterruptedException {
        zkClient.create(createNodeName+"/a", "this is api test1 data ch".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * del命令
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testDel() throws KeeperException, InterruptedException {
        zkClient.delete(createNodeName,-1);
        System.out.println("success to del "+createNodeName+" Znode！");
    }


    /**
     * get
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testGet() throws KeeperException, InterruptedException {
        byte[] data = zkClient.getData(createNodeName, false, null);
        System.out.println("节点"+createNodeName+"的数据是："+new String(data));
    }

    /**
     * 测试watch
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testWatch() throws KeeperException, InterruptedException {

        //先重置zkClient的watch对象（也可以在实例化zkClient时指定）
        zkClient.register(watcher);
        //进行监听
        byte[] data = zkClient.getData(createNodeName, true, null);
        System.out.println("获得节点"+createNodeName+"的数据是："+new String(data));

        //第一次进行修改，触发watch
        this.testSet();

        //第二次进行修改，触发watch
        this.testSet();
        this.testCh();
    }

    /**
     * 关闭连接
     * @throws InterruptedException
     */
    @After
    public void close() throws InterruptedException {
        zkClient.close();
    }


    /**定义watch对象*/
    private Watcher watcher = new Watcher() {
        public int watchCount = 0;  //记录监听次数

        @Override
        public void process(WatchedEvent watchedEvent) {
            System.out.println("获得监听事件,path：" + watchedEvent.getPath() + ";state：" + watchedEvent.getState() + ";type：" + watchedEvent.getType());

            //循环重复监听
            try {
                zkClient.getData(createNodeName, true,stat);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            watchCount++;
            System.out.println("第 "+watchCount+" 次监听到！");
        }
    };

}

