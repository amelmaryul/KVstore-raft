import java.util.ArrayList;
import java.util.List;

public class LogManager{
    
    private List<LogEntry> log = new ArrayList<>();
    private volatile int committedIndex = 0;
    private int lastApplied = 0;
    Object lock = new Object();







    public LogEntry get(int index){
        synchronized (lock){
            if (index > log.size()-1 ){
                return null;
            }
            return log.get(index);
        }

    }

    public LogEntry getLastLog(){
        synchronized (lock){
            int size = size();
            return log.get(size-1); 
        }
    }

    public int size(){
        synchronized (lock){
            return log.size();
        }
    }

    public void append(LogEntry logEntry){
        synchronized (lock){
            log.add(logEntry);
        } 
    }

    public void append(int index, LogEntry logEntry){
        synchronized (lock){
            log.add(index, logEntry);
        }
    }


    public int getCommitIndex(){
        return this.committedIndex;
    }

    public void setCommitIndex(int index){
        this.committedIndex = index;
    }

    public int getLastApplied(){
        return lastApplied;
    }

    public void setLastApplied(int value){
        this.lastApplied = value;
        
    }










    
}