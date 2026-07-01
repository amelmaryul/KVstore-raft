import java.io.Serializable;

public class LogEntry implements Serializable {
    String[] command;
    int term;
    int index;

    public LogEntry(String[] command, int term, int index){
        this.command = command;
        this.term = term;
        this.index = index;
    }
    
}
