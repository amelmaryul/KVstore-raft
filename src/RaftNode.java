import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RaftNode {

    String nodeId;
    List<Integer> ports;
    List<String> nodes;
    StorageEngine storageEngine;
    RaftState raftState;
    LogManager logManager;
    ElectionManager electionManager;
    HeartbeatTracker heartbeatTracker;
    RaftMessaging raftMessaging;
    ReplicationState replicationState;
    LeaderReplicationManager leaderReplicationManager;
    RequestHandler requestHandler;
    ReplicationManager replicationManager;
    FileStore fileStore;
    HeartbeatManager heartbeatManager;

    public RaftNode(String nodeId){
        this.nodeId = nodeId;

        this.fileStore = new FileStore(nodeId);
        ports = new ArrayList<>(Arrays.asList(5051,5052,5053, 5054, 5055));
        nodes = new ArrayList<>(Arrays.asList(System.getenv("NODES").split(",")));
        storageEngine = StorageEngine.getInstance();
        raftState = new RaftState(fileStore);
        logManager = new LogManager(fileStore);
        heartbeatTracker = new HeartbeatTracker();
        raftMessaging = new RaftMessaging();
        replicationState = new ReplicationState(logManager, nodes);

        electionManager = new ElectionManager(raftState, nodeId, nodes, logManager, raftMessaging);
        leaderReplicationManager = new LeaderReplicationManager(raftMessaging, replicationState, logManager, raftState, nodeId);
        requestHandler = new RequestHandler(raftState, logManager, heartbeatTracker, fileStore);
        replicationManager = new ReplicationManager(logManager);
        heartbeatManager = new HeartbeatManager(raftState, logManager, raftMessaging, replicationState, nodeId, nodes);


        System.out.println("Term: " + String.valueOf(raftState.getCurrentTerm()));
    }

    public void start(){
        Thread thread = new Thread(new RaftServerThread(nodeId, requestHandler, replicationManager));
        thread.start();



        new Thread(() -> {
            try{
                while (true){
                    String[] command = storageEngine.queue.take();
                    logManager.append(new LogEntry(command, raftState.getCurrentTerm(), logManager.size()));
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }).start();

        new Thread(() -> {
            while (true) {
                while (logManager.getLastApplied() < logManager.getCommitIndex()) {
                    System.out.println("[Local Replication Manager] trying to update the RSM!");
                    LogEntry entry = logManager.get(logManager.getLastApplied() + 1); 
                    if (entry != null){
                        String[] command = entry.command;

                        if (command[0].equals("set")) {
                            storageEngine.set(command[1], command[2]);
                            System.out.println("[Local Replicaotn Manager] Updated RSM");
                        } else if (command[0].equals("delete")) {
                            storageEngine.delete(command[1]);
                        }

                        logManager.setLastApplied(logManager.getLastApplied() + 1); 
                    }
                    else System.out.println("[Local Replication Manager] Can't update current logs is behind commit index!");
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        heartbeatTracker.updateHeartbeat();
        while (true){
            String role = raftState.getRole();

            if (role.equals("Follower")){
                if (heartbeatTracker.isExpired()){
                    raftState.setRole("Candidate");
                }
                else {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            else if (role.equals("Candidate")){
                System.out.println("Starting Election");
                boolean isLeader = electionManager.startElection();
                if (isLeader){
                    raftState.setRole("Leader");
                    System.out.println("[Leader] I am the Leader. Term: " + String.valueOf(raftState.getCurrentTerm()));
                    
                    // tell gateway im leader
                   try {
                        System.out.println("Trying to connect to socket at addy: " + System.getenv("GATEWAY_ID"));
                        Socket s = new Socket(System.getenv("GATEWAY_ID"), 5050);
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
                        pw.println(this.nodeId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    replicationState.reInitializeState();
                }
                else {
                    System.out.println("Election Lost");
                    raftState.setRole("Follower");
                    heartbeatTracker.updateHeartbeat();
                    heartbeatTracker.updateElectionTimeout();
                }
            } 

            else if (role.equals("Leader")) 
            { // sending heartbeats.
                heartbeatManager.start();
                updateCommit();
            }
        }
    }






    public void updateCommit(){
        synchronized (replicationState){
            replicationState.setMatchIndex(nodeId, logManager.size()-1);
            int n = nodes.size();
            int[] arr = new int[n];

            for (int i = 0; i < n; i++){
                arr[i] = replicationState.getMatchIndex(nodes.get(i));
            }
            Arrays.sort(arr);

            int mid = n /2;

            logManager.setCommitIndex(arr[mid]);
        }
    }



    
    public static void main(String[] args){
        System.out.println("RaftServer Open at Port: " + System.getenv("NODE_ID"));
        RaftNode node = new RaftNode(System.getenv("NODE_ID"));
        node.start();
    }
}
