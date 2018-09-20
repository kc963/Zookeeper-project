package com.zookeeperproject.app;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.Scanner;

public class Player {

    private static ZooKeeper zk;
    private static ConnectionCreator zkcc;

    public static void initialize(String host){
        zkcc = new ConnectionCreator();
        try{
            zk = zkcc.connect(host);
            System.out.println("Connection Established.");
        }catch(Exception e){
            System.out.println("Connection failed. Exiting the program.");
            System.exit(0);
        }
        try{
            if(zk.exists("/kchopra", false) == null){
                zk.create("/kchopra", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }catch(Exception e){
            System.out.println("There was some problem with loading the scoreboard. Exiting the program.");
            System.exit(0);
        }
    }

    private static void join(String name, String path) throws Exception{
        if(zk.exists(path, false) == null){
            zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }else{
            System.out.println("User already exists. Exiting the program.");
            System.exit(0);
        }

    }

    public static void scanScore(String name, Scanner sc, String path){
        System.out.println("Enter the score: ");
        int score = 0;
        try {
            score = sc.nextInt();
        }catch(Exception e){
            System.out.println("Invalid input. Exiting the program.");
            System.exit(0);
        }
        postScore(name, score, path);
    }

    private static void postScore(String name, int score, String path){
        try {
            zk.setData(path, Integer.toString(score).getBytes(), zk.exists(path, true).getVersion());
            String masterdata = new String(zk.getData("/kchopra", true, zk.exists("/kchopra", true)));
            masterdata += "#" + name + ":" +score + "#";
            zk.setData("/kchopra",masterdata.getBytes(),zk.exists("/kchopra", true).getVersion());
        }catch(Exception e){
            System.out.println("Some error occurred in posting score to the scoreboard. Exiting the program.");
            System.exit(0);
        }
    }

    private static void leave(){

    }

    public static void main(String args[]) throws Exception{
        initialize(args[1]);
        Scanner sc = new Scanner(System.in);
        String name = "";
        String path = "/kchopra/";
        if(args.length == 3){
            name = args[2];
            path += name;
            System.out.println(name + "\t" + path);
            join(name, path);
            do{
                scanScore(name, sc, path);
            }while(true);
        }else if(args.length == 4){
            name = args[2] + " " + args[3];
            path += name;
            join(name, path);
            do{
                scanScore(name, sc, path);
            }while(sc.hasNext());
        }else if(args.length == 6){
            name = args[2];
            path += name;
        }else {
            System.out.println("Invalid parameters received. Exiting the program.");
            System.exit(0);
        }

    }


}
