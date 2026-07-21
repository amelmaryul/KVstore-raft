package raft;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicationState {
    LogManager logManager;
    List<String> nodes;

    private final Map<String, Integer> nextIndex = new ConcurrentHashMap<>();
    private final Map<String, Integer> matchIndex = new ConcurrentHashMap<>();

    public ReplicationState(LogManager logManager, List<String> nodes){
        this.logManager = logManager;
        this.nodes = nodes;
    }


    public synchronized void reInitializeState(){
        synchronized (logManager.lock){
            int size = logManager.size();
            for (String node: nodes){
                matchIndex.put(node, 0);
                nextIndex.put(node, size);
            }
        }
    }


    public int getNextIndex(String port){
        return nextIndex.get(port);
    }

    public void setNextIndex(String port, int index){
        nextIndex.put(port, index);
    }

    public int getMatchIndex(String port){
        return matchIndex.get(port);
    }

    public void setMatchIndex(String port, int index){
        matchIndex.put(port, index);
    }
    
}
