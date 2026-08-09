package raft;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rpc.AppendEntriesRequest;
import rpc.AppendEntriesResponse;

public class HeartbeatManager {
    volatile boolean running = false;
    private Map<String, HeartbeatWorker> workers = new HashMap<>();
    List<String> nodes;
    String nodeId;
    RaftState raftState;
    LogManager logManager;
    RaftMessaging raftMessaging;
    ReplicationState replicationState;



    public HeartbeatManager(RaftState raftState, LogManager logManager, RaftMessaging raftMessaging, ReplicationState replicationState, String nodeId, List<String> nodes){
        this.raftState = raftState;
        this.logManager = logManager;
        this.raftMessaging = raftMessaging;
        this.replicationState = replicationState;
        this.nodeId = nodeId;
        this.nodes = nodes;
    }

    public void start(){
        if (running) return;
        running = true;
        for (String node : nodes){
            if (node.equals(nodeId)) continue;
            HeartbeatWorker worker = new HeartbeatWorker(raftState, logManager, raftMessaging, replicationState, nodeId, node);

            workers.put(node, worker);
            new Thread(worker).start();
        }


          
        new Thread(() ->{
            while (raftState.getRole().equals("Leader")){
                for (Map.Entry<String, HeartbeatWorker> entry : workers.entrySet()){
                    // check if each worker has not crashed or something
                }
            }
            running = false;
        }).start();
        



    }

    
}



class HeartbeatWorker implements Runnable {
    RaftState raftState;
    LogManager logManager;
    RaftMessaging raftMessaging;
    ReplicationState replicationState;
    String nodeId;
    String followerId;
    
    public HeartbeatWorker(RaftState raftState, LogManager logManager, RaftMessaging raftMessaging, ReplicationState replicationState, String nodeId, String followerId){
        this.raftState = raftState;
        this.logManager = logManager;
        this.raftMessaging = raftMessaging;
        this.replicationState = replicationState;
        this.nodeId = nodeId;
        this.followerId = followerId;
    }

    public void run(){
        while (raftState.getRole().equals("Leader")){
            try (Socket socket = new Socket()){
                socket.connect(new InetSocketAddress(followerId, 8081), 300);
                socket.setSoTimeout(2000);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                while (raftState.getRole().equals("Leader")){
                    List<LogEntry> entries = null;
                    int nextIndex = replicationState.getNextIndex(followerId);
                    LogEntry lg = logManager.get(nextIndex-1);
                    if (logManager.size() > nextIndex){
                        entries = logManager.getFrom(nextIndex);
                    }

                    AppendEntriesRequest req = new AppendEntriesRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term, entries, logManager.getCommitIndex());
                    AppendEntriesResponse response = (AppendEntriesResponse) raftMessaging.sendRequest(socket, out, in, req);


                    if (response.success && entries != null){
                        LogEntry lastEntry = entries.getLast();
                        replicationState.setNextIndex(followerId, lastEntry.index+1);
                        replicationState.setMatchIndex(followerId, lastEntry.index);
                    }

                    else if (!response.success && entries != null){
                        replicationState.setNextIndex(followerId, nextIndex-1);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } catch (Exception e){
                try {
                    Thread.sleep(300);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            }
        }


    }
}
