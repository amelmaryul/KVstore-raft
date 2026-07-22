package rpc;
public class RequestVoteResponse implements Message {
    public int term;
    public boolean voteGranted;


    public RequestVoteResponse(int term, boolean voteGranted){
        this.term = term;
        this.voteGranted = voteGranted;
    }
    
}
