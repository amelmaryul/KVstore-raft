package raft;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rpc.RequestVoteRequest;
import rpc.RequestVoteResponse;

public class ElectionManager {
    RaftState raftState;
    LogManager logManager;
    String nodeId;
    List<String> nodes;
    RaftMessaging raftMessaging;

    public ElectionManager(RaftState raftState, String nodeId, List<String> nodes, LogManager logManager, RaftMessaging raftMessaging){
        this.raftState = raftState;
        this.nodeId = nodeId;
        this.nodes = nodes;
        this.logManager = logManager;
        this.raftMessaging = raftMessaging;
    }


    public boolean startElection(){
        if (!raftState.becomeCandidate(nodeId)) return false;

        AtomicInteger votesReceived = new AtomicInteger(1);
        CountDownLatch latch = new CountDownLatch(nodes.size() -1);

        for (String node : nodes){
            if (node.equals(this.nodeId)) continue;

                new Thread(() -> {

                    try{
                        int count = 0;
                        LogEntry lg = logManager.getLastLog();
                        RequestVoteRequest req = new RequestVoteRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term);
                        RequestVoteResponse response = (RequestVoteResponse) raftMessaging.sendRequest(node, req);


                        if (response != null && response.voteGranted){
                            count = votesReceived.incrementAndGet();
                            System.out.println("Voted recieved current count: " + String.valueOf(count));

                        }  
                        else if (response != null && response.term > raftState.getCurrentTerm()){
                            System.out.println("My term is behind and will update it");
                            raftState.setTerm(response.term, null);
                        }


                    } finally {
                        latch.countDown();
                    }

                }).start();
        }        

        try{

            latch.await(500, TimeUnit.MILLISECONDS);
            if (votesReceived.get() > nodes.size() / 2 && raftState.getRole().equals(("Candidate"))){
                return true;
            }


        } catch (Exception e){
            e.printStackTrace();
        }
        
        return false;

    }
    
}
