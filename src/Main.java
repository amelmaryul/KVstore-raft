import java.net.InetAddress;

import client.ClientServer;
import raft.RaftNode;

public class Main {






    public static void main(String[] args){
        System.out.println("v4");

        new Thread(() -> ClientServer.main(args)).start();
        new Thread(() -> RaftNode.main(args)).start();
   
    }
    
}
