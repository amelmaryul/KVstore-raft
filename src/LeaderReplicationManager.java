import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;

public class LeaderReplicationManager {
    RaftMessaging raftMessaging;
    ReplicationState replicationState;
    LogManager logManager;
    RaftState raftState;
    String nodeId;
    
    public LeaderReplicationManager(RaftMessaging raftMessaging, ReplicationState replicationState, LogManager logManager, RaftState raftState, String nodeId){
        this.raftMessaging = raftMessaging;
        this.replicationState = replicationState;
        this.logManager = logManager;
        this.raftState = raftState;
        this.nodeId = nodeId;
    }


    public boolean sendLogs(String node, List<LogEntry> entries){
        try (Socket socket = new Socket()) {

            socket.setSoTimeout(500);

            SocketAddress address = new InetSocketAddress(node, 8081);
            socket.connect(address, 250);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            int nextIndex = replicationState.getNextIndex(node);
            LogEntry lg = logManager.get(nextIndex-1);
            LogEntry lastEntry = entries.getLast();

            AppendEntriesRequest req = new AppendEntriesRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term, entries, logManager.getCommitIndex());

            AppendEntriesResponse response = (AppendEntriesResponse) raftMessaging.sendRequest(socket, out, in, req);


            while (!response.success){

                replicationState.setNextIndex(node, nextIndex-1);
                nextIndex = replicationState.getNextIndex(node);

                entries = logManager.getFrom(nextIndex);

                lg = logManager.get(nextIndex-1);

                req = new AppendEntriesRequest(raftState.getCurrentTerm(), nodeId, lg.index, lg.term, entries, logManager.getCommitIndex());
                response = (AppendEntriesResponse) raftMessaging.sendRequest(socket, out, in, req);
            }
            lastEntry = entries.getLast();
            replicationState.setMatchIndex(node, lastEntry.index);
            replicationState.setNextIndex(node, lastEntry.index+1);
            // maybe also update commit idk. 
            System.out.println("[LeaderReplicationManager] Successfully replicted to node at: " + node);
            return true;



        } catch (NoRouteToHostException e){
            System.out.println("No route to host exception!");
            return false; 
        
        } catch (SocketTimeoutException e){
            System.out.println("Node at: " + node + " is not responding. Replication Failed");
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        
    }
    
}
