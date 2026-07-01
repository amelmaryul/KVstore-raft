public class AppendEntriesResponse implements Message {
    int term;
    boolean success;

    public AppendEntriesResponse(int term, boolean success){
        this.term = term;
        this.success = success;
    }
    
}
