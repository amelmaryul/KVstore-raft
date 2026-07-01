public class RequestVoteRequest implements Message{
    int term;
    int candidateId;
    int lastLogIndex;
    int lastLongTerm;

    public RequestVoteRequest(int term, int candidateId, int lastLogIndex, int lastLongTerm){
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLongTerm = lastLongTerm;
    }
    
}
