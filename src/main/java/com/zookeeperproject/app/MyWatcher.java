package com.zookeeperproject.app;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyWatcher implements Watcher {
    static ZooKeeper zk;
    static int c = 0;
    static String main_path = "/kchopra";
    static int N = 0;


    public void process(WatchedEvent watchedEvent) {
        //System.out.println("\t" + c++ + " : " + readNode(main_path));
        displayWork(main_path);

    }

    public void displayWork(String path){
        TreeMap<Integer, ArrayList<String>> map = new TreeMap<Integer, ArrayList<String>>();
        Stack<String> st = new Stack<String>();
        String data = readNode(path);
        //System.out.println("Data : " + data);
        String[] entries = data.split("##");
        ArrayList<String> list;
        if(N > entries.length){
            N = entries.length;
        }
        for(int i= 0; i<entries.length; i++){
            String s = entries[i];
            if(i==0){
                s = s.substring(1);
            }else if(i== entries.length-1){
                s = s.substring(0, s.indexOf('#'));
            }
            //System.out.println("data: " + s);
            String[] words = s.split(":");
            String name = words[0];
            String score = words[1];
            String stack_data = name + "\t\t" + score;
            st.push(stack_data);
            int key = Integer.parseInt(score);
            if(map.containsKey(key)){
                list = map.get(key);
                list.add(name);
            }else{
                list = new ArrayList<String>();
                list.add(name);
            }
            map.put(key, list);
        }
        printHighestScores(map);
        System.out.println();
        printMostRecentScores(st);
        System.out.println();
    }

    public String readNode(String path){
        String data = "";
        try {
            for (byte b : zk.getData(path, new MyWatcher(), zk.exists(path, true))){
                data += (char)b;
            }
        } catch (Exception e) {
            System.out.println("An error occurred while reading the znodes. Exiting the program.");
            System.exit(0);
        }
        return data;
    }

    public void printHighestScores(TreeMap<Integer, ArrayList<String>> map){
        Stack<String> stack = new Stack<String>();
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry data = (Map.Entry)iterator.next();
            ArrayList<String> list = (ArrayList)data.getValue();
            for(String str : list){
                String stack_data = str + "\t\t" + data.getKey();
                stack.push(stack_data);
            }
        }
        System.out.println("Highest Scores");
        System.out.println("--------------");
        for(int i=0; i<N; i++){
            System.out.println(stack.pop());
        }
    }

    public void printMostRecentScores(Stack<String> st){
        System.out.println("Most Recent Scores");
        System.out.println("------------------");
        for(int i=0; i<N; i++){
            System.out.println(st.pop());
        }
    }

    public static void main(String args[]) throws Exception {
        String path = "";
        try{
            path = args[1];
            N = Integer.parseInt(args[2]);
        }catch(Exception e){
            System.out.println("Invalid parameter list. Exiting the program.");
            System.exit(0);
        }
        try {
            zk = new ConnectionCreator().connect(path);
        }catch(Exception e){
            System.out.println("Couldn't connect to the server. Exiting the program.");
            System.exit(0);
        }
        MyWatcher obj = new MyWatcher();
        final CountDownLatch connSignal = new CountDownLatch(1);
        try {
            //System.out.println("running display");
            obj.displayWork(main_path);
            //System.out.println("Printing numbers");
            zk.getData("/kchopra", obj, zk.exists("/kchopra", true));
            connSignal.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        }catch(Exception e) {
            System.out.println("Node doesn't exist");
            e.printStackTrace();
        }
    }
}
