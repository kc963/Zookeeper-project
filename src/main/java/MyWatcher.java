import org.apache.zookeeper.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyWatcher implements Watcher {
    static ZooKeeper zk;
    static int c = 0;
    static String main_path = "/kchopra";
    static int N = 0;
    static ArrayList<String> names;

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
        names = new ArrayList<String>();
    }

    public void process(WatchedEvent watchedEvent) {
        //System.out.println("\t" + c++ + " : " + readNode(main_path));
        //names.add("KC");
        populateNames(main_path);
        displayWork(main_path);

    }

    public void populateNames(String path){
        String data = readNode(path);
        if(data.length() != 0){
            String[] entries = data.split("@&");
            for(int i=0; i<entries.length; i++){
                //System.out.println(entries[i]);
                if(entries[i].length()>0 && entries[i].charAt(0) != '#'){
                    try {
                        if (zk.exists("/kchopra/"+entries[i], false) != null) {
                            if (!names.contains(entries[i])) {
                                names.add(entries[i]);
                            }
                        } else {
                            if (names.contains(entries[i])) {
                                names.remove(entries[i]);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //System.out.println(names);
            //System.out.println("Names populated");
        }
    }

    public void displayWork(String path){
        TreeMap<Integer, ArrayList<String>> map = new TreeMap<Integer, ArrayList<String>>();
        Stack<String> st = new Stack<String>();
        String data = readNode(path);
        if(data.length() > 0) {
            //System.out.println("Data : " + data);

            String[] entries = data.split("#");
            ArrayList<String> list;

            //populateNames("/kchopra");
            for (int i = 0; i < entries.length; i++) {
                 String en[] = entries[i].split("#");
                 for(int j=0; j<en.length; j++) {
                     String s = en[j];
                     //System.out.println(s);
                     if (s.length() == 0 || s.indexOf(":")==-1) {
                         continue;
                     }

                     try{
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
                     }catch(Exception e){
                         //System.out.println("Caught");
                     }
                 }
            }
//            if (N > st.size()) {
//                System.out.println("There aren't " + N + " records present in the server. Resetting the size to " + st.size());
//                N = st.size();
//            }
            printHighestScores(map);
            System.out.println();
            printMostRecentScores(st);
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

    public void printHighestScores(TreeMap<Integer, ArrayList<String>> map){
        //System.out.println(names);
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
        int i=0;
        while(i<N && stack.size()!=0){
            System.out.println(stack.pop());
        }
    }

    public void printMostRecentScores(Stack<String> st){
        //System.out.println(names);
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
        int i=0;
        while(i<N && solution.size()!=0){
            System.out.println(solution.remove());
        }
    }

    public static void main(String args[]) {
        MyWatcher obj = new MyWatcher();
        String host = "";
        try{
            host = args[1];
            N = Integer.parseInt(args[2]);
        }catch(Exception e){
            System.out.println("Invalid parameter list. Exiting the program.");
            System.exit(0);
        }
        obj.initialize(host);
        obj.populateNames("/kchopra");
        try {
            zk = new ConnectionCreator().connect(host);
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
