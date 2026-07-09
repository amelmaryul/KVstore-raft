import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    int nodeId;
    List<Integer> ports;
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

    public RaftNode(int nodeId){
        this.nodeId = nodeId;

        ports = new ArrayList<>(Arrays.asList(5051,5052,5053, 5054, 5055));
        storageEngine = StorageEngine.getInstance();
        raftState = new RaftState();
        logManager = new LogManager();
        heartbeatTracker = new HeartbeatTracker();
        raftMessaging = new RaftMessaging();
        replicationState = new ReplicationState(logManager, ports);

        electionManager = new ElectionManager(raftState, nodeId, ports, logManager, raftMessaging);
        leaderReplicationManager = new LeaderReplicationManager(raftMessaging, replicationState, logManager, raftState, nodeId);
        requestHandler = new RequestHandler(raftState, logManager, heartbeatTracker);
        replicationManager = new ReplicationManager(logManager);
    }

    public void start(){
        Thread thread = new Thread(new RaftServerThread(nodeId, requestHandler, replicationManager));
        thread.start();


/* 
        new Thread(() -> {
            try{

                while (true){
                    String[] command = storageEngine.queue.take();
                    logManager.append(new LogEntry(command, raftState.getCurrentTerm(), logManager.size()));
                    if (raftState.getRole().equals("Leader")){
                        for (int port : ports){
                        new Thread(() -> {
                                leaderReplicationManager.sendLogs(port, logManager.getFrom(port));
                        }).start();
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }).start();

        new Thread(() -> {
            while (true) {
                while (logManager.getLastApplied() < logManager.getCommitIndex()) {
                    LogEntry entry = logManager.get(logManager.getLastApplied() + 1);
                    String[] command = entry.command;

                    if (command[0].equals("set")) {
                        storageEngine.set(command[1], command[2]);
                    } else if (command[0].equals("delete")) {
                        storageEngine.delete(command[1]);
                    }

                    logManager.setLastApplied(logManager.getLastApplied() + 1);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
*/

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
                boolean isLeader = electionManager.startElection();
                if (isLeader){
                    raftState.setRole("Leader");
                    System.out.println("[Leader] I am the Leader. Term: " + String.valueOf(raftState.getCurrentTerm()));
                }
                else {
                    raftState.setRole("Follower");
                    heartbeatTracker.updateHeartbeat();
                }
            } 

            else if (role.equals("Leader")) //leader.doleaderActivity(); // should shit be a mega objects that calls smaller objects that handle all the leader stuff. i would need a lot though. i would need something that can hold matchindex and all that stuff. im gonna need somethign to manage the raftmessaging system and handle all that stuff theres a lot to do it feels like
            {
                for (int port : ports){
                    if (port == nodeId) continue;

                    new Thread(() -> {
                        LogEntry lg = logManager.getLastLog();
                        AppendEntriesRequest req = new AppendEntriesRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term, null, logManager.getCommitIndex());
                        AppendEntriesResponse response = (AppendEntriesResponse) raftMessaging.sendRequest(port, req);

                    }).start();
                }
                
                 try {
                        Thread.sleep(75);
                    } catch (Exception e){
                        e.printStackTrace();
                }

            }
        }
    }



    
    public static void main(String[] args){
        System.out.println("RaftServer Open at Port: " + String.valueOf(args[1]));
        RaftNode node = new RaftNode(Integer.valueOf(args[1]));
        node.start();
    }
}
