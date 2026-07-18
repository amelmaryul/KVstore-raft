public class RequestVoteRequest implements Message{
    int term;
    String candidateId;
    int lastLogIndex;
    int lastLogTerm;

    public RequestVoteRequest(int term, String candidateId, int lastLogIndex, int lastLogTerm){
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
    
}
