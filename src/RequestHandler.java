public class RequestHandler {
    RaftState raftState;
    LogManager logManager;

    public RequestHandler(RaftState raftState, LogManager logManager){
        this.raftState = raftState;
        this.logManager = logManager;
    }

    public RequestVoteResponse handleRequestVote(RequestVoteRequest req){
        synchronized (raftState.getLock()){
            if (req.term > raftState.getCurrentTerm()){
                raftState.setTerm(req.term);
            }
            if (req.term == raftState.getCurrentTerm() && raftState.getVotedFor() == null){
                raftState.setVotedFor(req.candidateId);
                return new RequestVoteResponse(raftState.getCurrentTerm(), true);
            }

            return new RequestVoteResponse(raftState.getCurrentTerm(), false);
        }
    }



    public AppendEntriesResponse handleAppendEntries(AppendEntriesRequest req){
        synchronized (raftState.getLock()){
            if (req.term > raftState.getCurrentTerm()){
                raftState.setTerm(req.term);
            }
            if (req.entries == null && req.term == raftState.getCurrentTerm()){
                return new AppendEntriesResponse(raftState.getCurrentTerm(), true);
            }
        }
        synchronized (logManager.lock){
            LogEntry lg = logManager.getLastLog();
            if (req.leaderCommit > logManager.getCommitIndex()) logManager.setCommitIndex(req.leaderCommit);
            if (req.prevLogIndex == lg.index && req.prevLogTerm == lg.term){
                return new AppendEntriesResponse(raftState.getCurrentTerm(), true);
            }

            return new AppendEntriesResponse(raftState.getCurrentTerm(), false);
        }
    }
}