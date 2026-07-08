import java.util.List;

public class ReplicationManager {
    LogManager logManager;

    public ReplicationManager(LogManager logManager){
        this.logManager = logManager;
    }

    public void replicate(List<LogEntry> entries){
        if (entries == null) return;
        synchronized (logManager.lock){
            LogEntry firsEntry = entries.get(0);
            LogEntry lastEntry = entries.get(entries.size()-1);

            for (int i = firsEntry.index; i<lastEntry.index +1; i++){
                logManager.append(i, lastEntry);
            }
        }
    }
    
}
