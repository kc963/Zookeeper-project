package com.zookeeperproject.app;

import org.apache.zookeeper.ZooKeeper;

public class ConnectionCreator {
    private ZooKeeper zk;

    public ZooKeeper connect(String host) throws Exception{
        zk = new ZooKeeper(host, 10000, null);
        return zk;
    }

    public void close() throws Exception{
        zk.close();
    }
}
