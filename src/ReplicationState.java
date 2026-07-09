import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicationState {
    LogManager logManager;
    List<Integer> ports;

    private final Map<Integer, Integer> nextIndex = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> matchIndex = new ConcurrentHashMap<>();

    public ReplicationState(LogManager logManager, List<Integer> ports){
        this.logManager = logManager;
        this.ports = ports;
    }


    public synchronized void reInitializeState(){
        synchronized (logManager.lock){
            int size = logManager.size();
            for (int port: ports){
                matchIndex.put(port, 0);
                nextIndex.put(port, size);
            }
        }
    }


    public int getNextIndex(int port){
        return nextIndex.get(port);
    }

    public void setNextIndex(int port, int index){
        nextIndex.put(port, index);
    }

    public int getMatchIndex(int port){
        return matchIndex.get(port);
    }

    public void setMatchIndex(int port, int index){
        matchIndex.put(port, index);
    }
    
}
