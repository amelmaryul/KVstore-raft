import java.util.ArrayList;
import java.util.List;

public class LogManager{
    
    private List<LogEntry> log = new ArrayList<>();
    private volatile int committedIndex = 0;
    private int lastApplied = 0;
    Object lock = this;
    FileStore fileStore;


    public LogManager(FileStore fileStore){
        this.fileStore = fileStore;
        log = fileStore.initializeLogs();
        if (log == null) log = new ArrayList<>();
        log.add(0, new LogEntry(null, 0, 0));
    }




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
            int size = this.size();
            return this.get(size-1); 
        }
    }

    public List<LogEntry> getFrom(int startIndex){
        List<LogEntry> res = new ArrayList<>();
        synchronized (lock){
            for (; startIndex < log.size(); startIndex++){
                res.add(log.get(startIndex));
            }
            return res;
        }
       
    }

    public int size(){
        synchronized (lock){
            return log.size();
        }
    }

    public void append(LogEntry logEntry){
        synchronized (lock){
            fileStore.appendLog(logEntry);
            log.add(logEntry);
        } 
    }

    public void append(int index, LogEntry logEntry){
        synchronized (lock){
            fileStore.appendLog(logEntry);
            log.add(index, logEntry);
        }
    }

    public void truncate(int index){
        synchronized (lock){
            while (log.size() > index){
                log.removeLast();
            }
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