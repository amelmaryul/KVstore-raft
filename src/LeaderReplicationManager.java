import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class LeaderReplicationManager {
    RaftMessaging raftMessaging;
    ReplicationState replicationState;
    LogManager logManager;
    RaftState raftState;
    int nodeId;
    
    public LeaderReplicationManager(RaftMessaging raftMessaging, ReplicationState replicationState, LogManager logManager, RaftState raftState, int nodeId){
        this.raftMessaging = raftMessaging;
        this.replicationState = replicationState;
        this.logManager = logManager;
        this.raftState = raftState;
        this.nodeId = nodeId;
    }


    public boolean sendLogs(int port, List<LogEntry> entries){
        try (Socket socket = new Socket("localhost", port)) {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            int nextIndex = replicationState.getNextIndex(port);
            LogEntry lg = logManager.get(nextIndex-1);
            LogEntry lastEntry = entries.getLast();

            AppendEntriesRequest req = new AppendEntriesRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term, entries, logManager.getCommitIndex());

            AppendEntriesResponse response = (AppendEntriesResponse) raftMessaging.sendRequest(socket, out, in, req);


            while (!response.success){

                replicationState.setNextIndex(port, nextIndex-1);
                entries = logManager.getFrom(nextIndex);

                nextIndex = replicationState.getNextIndex(port);
                lg = logManager.get(nextIndex-1);

                req = new AppendEntriesRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term, entries, logManager.getCommitIndex());
                response = (AppendEntriesResponse) raftMessaging.sendRequest(socket, out, in, req);
            }
            replicationState.setMatchIndex(port, lastEntry.index);
            replicationState.setNextIndex(port, lastEntry.index+1);
            // maybe also update commit idk. 
            return true;



        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        
    }
    
}
