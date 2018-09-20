package com.zookeeperproject.app;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class MyWatcher implements Watcher {

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent.getPath());
        System.out.println(watchedEvent.getState());
        System.out.println(watchedEvent.getType());
        System.out.println(watchedEvent.toString());
        System.out.println();

    }

    public static void main(String args[]) throws Exception {
        ZooKeeper zk = new ConnectionCreator().connect("localhost");
        for (byte b : zk.getData("/kchopra", new MyWatcher(), zk.exists("/kchopra", true))){
            System.out.println((char)b);
        }

    }
}
