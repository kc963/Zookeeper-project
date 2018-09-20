package com.zookeeperproject.app;

import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

public class Tester {
    private static ZooKeeper zk;
    private static ConnectionCreator zkcc;

    public static void main(String args[]) throws Exception{
        zkcc = new ConnectionCreator();
        zk = zkcc.connect("localhost");

        List<String> znodes = new ArrayList<String>();
        znodes = zk.getChildren("/", true);

        for(String s: znodes){
            System.out.print(" [" + s + "] ");
        }
    }
}
