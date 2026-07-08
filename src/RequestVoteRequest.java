public class RequestVoteRequest implements Message{
    int term;
    int candidateId;
    int lastLogIndex;
    int lastLogTerm;

    public RequestVoteRequest(int term, int candidateId, int lastLogIndex, int lastLogTerm){
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
    
}
