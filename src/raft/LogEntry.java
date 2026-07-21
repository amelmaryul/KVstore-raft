package raft;
import java.io.Serializable;

public class LogEntry implements Serializable {
    public String[] command;
    public int term;
    public int index;

    public LogEntry(String[] command, int term, int index){
        this.command = command;
        this.term = term;
        this.index = index;
    }
    
}
