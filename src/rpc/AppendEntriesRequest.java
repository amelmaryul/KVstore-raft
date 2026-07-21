package rpc;
import java.util.List;

import raft.LogEntry;

public class AppendEntriesRequest implements Message {
    public int term;
    public String leaderId;
    public int prevLogIndex;
    public int prevLogTerm;
    public List<LogEntry> entries;
    public int leaderCommit;

   public AppendEntriesRequest(int term, String leaderId, int prevLogIndex, int prevLogTerm, List<LogEntry> entries, int leaderCommit){
    this.term = term;
    this.leaderId = leaderId;
    this.prevLogIndex = prevLogIndex;
    this.prevLogTerm = prevLogTerm;
    this.entries = entries;
    this.leaderCommit = leaderCommit;

   } 
}
