import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ElectionManager {
    RaftState raftState;
    LogManager logManager;
    int nodeId;
    List<Integer> ports;
    RaftMessaging raftMessaging;

    public ElectionManager(RaftState raftState, int nodeId, List<Integer> ports, LogManager logManager, RaftMessaging raftMessaging){
        this.raftState = raftState;
        this.nodeId = nodeId;
        this.ports = ports;
        this.logManager = logManager;
        this.raftMessaging = raftMessaging;
    }


    public boolean startElection(){
        if (!raftState.becomeCandidate(nodeId)) return false;

        AtomicInteger votesReceived = new AtomicInteger(1);
        CountDownLatch latch = new CountDownLatch(ports.size() -1);

        for (int port : ports){
            if (port == this.nodeId) continue;

                new Thread(() -> {

                    try{
                        int count = 0;
                        LogEntry lg = logManager.getLastLog();
                        RequestVoteRequest req = new RequestVoteRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term);
                        RequestVoteResponse response = (RequestVoteResponse) raftMessaging.sendRequest(port, req);


                        if (response != null && response.voteGranted){
                            count = votesReceived.incrementAndGet();
                            System.out.println("Voted recieved current count: " + String.valueOf(count));

                        }  
                        else if (response != null && response.term > raftState.getCurrentTerm()){
                            raftState.setTerm(response.term);
                        }


                    } finally {
                        latch.countDown();
                    }

                }).start();
        }        

        try{

            latch.await(500, TimeUnit.MILLISECONDS);
            if (votesReceived.get() > ports.size() / 2 && raftState.getRole().equals(("Candidate"))){
                return true;
            }


        } catch (Exception e){
            e.printStackTrace();
        }
        
        return false;

    }
    
}
