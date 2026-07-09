public class RequestHandler {
    RaftState raftState;
    LogManager logManager;
    HeartbeatTracker heartbeatTracker;

    public RequestHandler(RaftState raftState, LogManager logManager, HeartbeatTracker heartbeatTracker){
        this.raftState = raftState;
        this.logManager = logManager;
        this.heartbeatTracker = heartbeatTracker;
    }

    public RequestVoteResponse handleRequestVote(RequestVoteRequest req){
        heartbeatTracker.updateHeartbeat();
        synchronized (raftState.getLock()){
            if (req.term > raftState.getCurrentTerm()){
                synchronized (raftState.getLock()){
                    raftState.setTerm(req.term);
                    raftState.setRole("Follower");
                }
            }
            if (req.term == raftState.getCurrentTerm() && raftState.getVotedFor() == null){
                raftState.setVotedFor(req.candidateId);
                return new RequestVoteResponse(raftState.getCurrentTerm(), true);
            }

            return new RequestVoteResponse(raftState.getCurrentTerm(), false);
        }
    }



    public AppendEntriesResponse handleAppendEntries(AppendEntriesRequest req){
        heartbeatTracker.updateHeartbeat();
        synchronized (raftState.getLock()){
            if (req.term > raftState.getCurrentTerm()){
                raftState.setTerm(req.term);
            }
            if (req.entries == null && req.term == raftState.getCurrentTerm()){
                return new AppendEntriesResponse(raftState.getCurrentTerm(), true);
            }
        }
        synchronized (logManager.lock){
            LogEntry lg = logManager.get(req.prevLogIndex);
            if (lg != null && req.leaderCommit > logManager.getCommitIndex()) logManager.setCommitIndex(req.leaderCommit);
            if (lg != null && req.prevLogIndex == lg.index && req.prevLogTerm == lg.term){
                return new AppendEntriesResponse(raftState.getCurrentTerm(), true);
            }

            return new AppendEntriesResponse(raftState.getCurrentTerm(), false);
        }
    }
}