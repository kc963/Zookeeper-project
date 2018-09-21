package com.zookeeperproject.app;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;
import org.apache.zookeeper.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyWatcher implements Watcher {
    static ZooKeeper zk;
    static int c = 0;
    static String main_path = "/kchopra";
    static int N = 0;

    public void initialize(String host){
        try {
            zk = new ConnectionCreator().connect(host);
        } catch (Exception e) {
            System.out.println("Couldn't connect to the Server. Exiting the program.");
            System.exit(0);
        }
        try {
            if(zk.exists(main_path, false) == null){
                zk.create(main_path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            System.out.println("An error occurred while creating the master znode. Exiting the system.");
            System.exit(0);
        }
    }

    public void process(WatchedEvent watchedEvent) {
        //System.out.println("\t" + c++ + " : " + readNode(main_path));
        displayWork(main_path);

    }

    public void displayWork(String path){
        TreeMap<Integer, ArrayList<String>> map = new TreeMap<Integer, ArrayList<String>>();
        Stack<String> st = new Stack<String>();
        ArrayList<String> names = new ArrayList<String>();
        String data = readNode(path);
        if(data.length() > 1) {
            //System.out.println("Data : " + data);

            String[] entries = data.split("#");
            ArrayList<String> list;

            for (int i = 0; i < entries.length; i++) {
                 String en[] = entries[i].split("#");
                 for(int j=0; j<en.length; j++) {
                     String s = en[j];
                     //System.out.println(s);
                     if (s.length() <= 0) {
                         continue;
                     }
                     if (s.indexOf(":") == -1) {
                         String[] str = s.split("@&");
                         s = str[1];
                         String node_path = "/" + s;
                         try {
                             if (zk.exists(node_path, false) != null) {
                                 if (!names.contains(s)) {
                                     names.add(s);
                                 }
                             } else {
                                 if (names.contains(s)) {
                                     names.remove(s);
                                 }
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                         continue;
                     }
                     //                if (i == 0) {
                     //                    s = s.substring(1);
                     //                } else if (i == entries.length - 1) {
                     //                    s = s.substring(0, s.indexOf('#'));
                     //                }
                     //System.out.println("data: " + s);
                     String[] words = s.split(":");
                     String name = words[0];
                     String score = words[1];
                     String stack_data = name + ":" + score;
                     st.push(stack_data);
                     int key = Integer.parseInt(score);
                     if (map.containsKey(key)) {
                         list = map.get(key);
                         list.add(name);
                     } else {
                         list = new ArrayList<String>();
                         list.add(name);
                     }
                     map.put(key, list);
                 }
            }
            if (N > st.size()) {
                System.out.println("There aren't " + N + " records present in the server. Resetting the size to " + st.size());
                N = st.size();
            }
            printHighestScores(map, names);
            System.out.println();
            printMostRecentScores(st, names);
            System.out.println();
        }
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

    public void printHighestScores(TreeMap<Integer, ArrayList<String>> map, ArrayList<String> names){
        Stack<String> stack = new Stack<String>();
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry data = (Map.Entry)iterator.next();
            ArrayList<String> list = (ArrayList)data.getValue();
            for(String str : list){
                String stack_data = str + "\t\t" + data.getKey();
                if(names.contains(str)){
                    stack_data += "  **";
                }
                stack.push(stack_data);
            }
        }
        System.out.println("Highest Scores");
        System.out.println("--------------");
        for(int i=0; i<N; i++){
            System.out.println(stack.pop());
        }
    }

    public void printMostRecentScores(Stack<String> st, ArrayList<String> names){
        Queue<String> solution = new LinkedList<String>();
        while(st.size() != 0){
            String data = st.pop();
            String name = data.split(":")[0];
            String score = data.split(":")[1];
            String line = name + "\t\t" + score;
            if(names.contains(name)){
                line += "  **";
            }
            solution.add(line);
        }
        System.out.println("Most Recent Scores");
        System.out.println("------------------");
        for(int i=0; i<N; i++){
            System.out.println(solution.remove());
        }
    }

    public static void main(String args[]) {
        MyWatcher obj = new MyWatcher();
        String path = "";
        try{
            path = args[1];
            N = Integer.parseInt(args[2]);
        }catch(Exception e){
            System.out.println("Invalid parameter list. Exiting the program.");
            System.exit(0);
        }
        obj.initialize(path);
        try {
            zk = new ConnectionCreator().connect(path);
        }catch(Exception e){
            System.out.println("Couldn't connect to the server. Exiting the program.");
            System.exit(0);
        }
        final CountDownLatch connSignal = new CountDownLatch(1);
        try {
            //System.out.println("running display");
            obj.displayWork(main_path);
            //System.out.println("Printing numbers");

        }catch(Exception e) {
            System.out.println("Node doesn't exist at present");
            //e.printStackTrace();
        }
        try {
            zk.getData("/kchopra", obj, zk.exists("/kchopra", true));
            connSignal.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("Couldn't connect to the server. Exiting the program.");
        }

    }
}
