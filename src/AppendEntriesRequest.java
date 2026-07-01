import java.util.List;

public class AppendEntriesRequest implements Message {
    int term;
    int leaderId;
    int prevLogIndex;
    int prevLogTerm;
    List<LogEntry> entries;
    int leaderCommit;

   public AppendEntriesRequest(int term, int leaderId, int prevLogIndex, int prevLogTerm, List<LogEntry> entries, int leaderCommit){
    this.term = term;
    this.leaderId = leaderId;
    this.prevLogIndex = prevLogIndex;
    this.prevLogTerm = prevLogTerm;
    this.entries = entries;
    this.leaderCommit = leaderCommit;

   } 
}
