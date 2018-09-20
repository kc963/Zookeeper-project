package com.zookeeperproject.app;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.Random;
import java.util.Scanner;

public class Player {

    private static ZooKeeper zk;
    private static ConnectionCreator zkcc;

    public static void initialize(String host, final String name){
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
        writeMasterName(name);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                System.out.println("Hooked");
                writeMasterName(name);
                System.out.println("Exit");
            }
        });
    }

    public static void writeMasterName(String name){
        try {
            String masterdata = new String(zk.getData("/kchopra", true, zk.exists("/kchopra", true)));
            masterdata += "$" + name + "$";
            zk.setData("/kchopra",masterdata.getBytes(),zk.exists("/kchopra", true).getVersion());
        }catch(Exception e){
            System.out.println("Some error occurred in posting score to the scoreboard. Exiting the program.");
            System.exit(0);
        }
    }

    private static void join(String name, String path) throws Exception{
        if(zk.exists(path, false) == null){
            zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
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
            System.out.println("1");
            zk.setData(path, Integer.toString(score).getBytes(), zk.exists(path, true).getVersion());
            System.out.println("2");
            String masterdata = new String(zk.getData("/kchopra", true, zk.exists("/kchopra", true)));
            System.out.println("3");
            masterdata += "#" + name + ":" +score + "#";
            System.out.println("4");
            zk.setData("/kchopra",masterdata.getBytes(),zk.exists("/kchopra", true).getVersion());
        }catch(Exception e){
            System.out.println("Some error occurred in posting score to the scoreboard. Exiting the program.");
            System.exit(0);
        }
    }

    private static void automation(String name, String path, int count, int u_delay, int u_score){
        Random rand = new Random();
        int score_sd = 1;
        if(u_score > 50){
            score_sd = (int)(0.1*u_score);
        }else{
            score_sd = (int)(0.2*u_score);
        }
        int sd = (int)(0.2*u_delay);
        for(int i=0; i<count; i++){
            int score = (int)Math.floor(rand.nextGaussian()*score_sd + u_score);
            int delay = (int)Math.floor(rand.nextGaussian()*sd + u_delay*1000);
            postScore(name,score,path);
            try {
                Thread.currentThread().sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws Exception{
        final Thread myThread = Thread.currentThread();
        Scanner sc = new Scanner(System.in);
        String path = "/kchopra/";
        if(args.length == 3){
            final String name = args[2];
            initialize(args[1], name);
            path += name;
            join(name, path);
            do{
                scanScore(name, sc, path);
            }while(true);
        }else if(args.length == 4){
            final String name = args[2] + " " + args[3];
            initialize(args[1], name);
            path += name;
            join(name, path);
            do{
                scanScore(name, sc, path);
            }while(true);
        }else if(args.length == 6){
            final String name = args[2];
            path+=name;
            initialize(args[1], name);
            join(name,path);
            automation(name, path, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        }else {
            System.out.println("Invalid parameters received. Exiting the program.");
            System.exit(0);
        }

    }


}
