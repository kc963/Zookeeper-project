package com.zookeeperproject.app;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyWatcher implements Watcher {
    static ZooKeeper zk;
    static int c = 0;
    public void process(WatchedEvent watchedEvent) {
        String data = "";
        try {
            for (byte b : zk.getData("/kchopra", new MyWatcher(), zk.exists("/kchopra", true))){
                data += (char)b;
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\t" + c++ + " : " + data);

    }

    public static void main(String args[]) throws Exception {
        zk = new ConnectionCreator().connect("localhost");
        MyWatcher obj = new MyWatcher();
        final CountDownLatch connSignal = new CountDownLatch(1);
        try {
            zk.getData("/kchopra", obj, zk.exists("/kchopra", true));
            connSignal.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        }catch(Exception e) {
            System.out.println("Node doesn't exist");
        }
    }
}
