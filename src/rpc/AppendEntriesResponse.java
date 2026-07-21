package rpc;
public class AppendEntriesResponse implements Message {
    public int term;
    public boolean success;

    public AppendEntriesResponse(int term, boolean success){
        this.term = term;
        this.success = success;
    }
    
}
