public class RequestVoteResponse implements Message {
    int term;
    boolean voteGranted;


    public RequestVoteResponse(int term, boolean voteGranted){
        this.term = term;
        this.voteGranted = voteGranted;
    }
    
}
