public class RequestHandler {
    RaftState raftState;
    LogManager logManager;
    HeartbeatTracker heartbeatTracker;
    ReplicationState replicationState;
    FileStore fileStore;

    public RequestHandler(RaftState raftState, LogManager logManager, HeartbeatTracker heartbeatTracker, FileStore fileStore){
        this.raftState = raftState;
        this.logManager = logManager;
        this.heartbeatTracker = heartbeatTracker;
        this.fileStore = fileStore;
    }

    public RequestVoteResponse handlerequestvote_justkeepherefornow(RequestVoteRequest req){
        synchronized (logManager.lock){
            synchronized (raftState.getLock()){

                if (req.term > raftState.getCurrentTerm()){
                    raftState.setTerm(req.term, null);
                    heartbeatTracker.updateHeartbeat();
                    return new RequestVoteResponse(raftState.getCurrentTerm(), true);
                    
                }
                else if (req.term == raftState.getCurrentTerm() && raftState.getVotedFor() == null){
                    heartbeatTracker.updateHeartbeat();
                    raftState.setVotedFor(req.candidateId);
                    return new RequestVoteResponse(raftState.getCurrentTerm(), true);
                }
                LogEntry lastEntry = logManager.getLastLog();
                if (req.lastLogTerm < lastEntry.term || (req.lastLogTerm == lastEntry.term && req.lastLogIndex < lastEntry.index)){
                    return new RequestVoteResponse(raftState.getCurrentTerm(), false);

                }

                return new RequestVoteResponse(raftState.getCurrentTerm(), false);
            }
        }
    }


    public RequestVoteResponse handleRequestVote(RequestVoteRequest req){
        synchronized (logManager.lock){
            synchronized (raftState.getLock()){
                if (req.term < raftState.getCurrentTerm()){
                    return new RequestVoteResponse(raftState.getCurrentTerm(), false);
                }
                if (req.term > raftState.getCurrentTerm()){
                    heartbeatTracker.updateHeartbeat();
                    raftState.setTerm(req.term, null);
                }

                LogEntry lastEntry = logManager.getLastLog();
                if ((raftState.getVotedFor() == null || raftState.getVotedFor().equals(req.candidateId)) && !(req.lastLogTerm < lastEntry.term || (req.lastLogTerm == lastEntry.term && req.lastLogIndex < lastEntry.index))){
                    heartbeatTracker.updateElectionTimeout();
                    raftState.setVotedFor(req.candidateId);
                    return new RequestVoteResponse(raftState.getCurrentTerm(), true);
                }

                return new RequestVoteResponse(raftState.getCurrentTerm(), false);
            }
        }
    }
    



    public AppendEntriesResponse handleAppendEntries_justkeepherefornow(AppendEntriesRequest req){
        heartbeatTracker.updateHeartbeat();
        synchronized (raftState.getLock()){
            if (req.term > raftState.getCurrentTerm()){
                raftState.setTerm(req.term, null);
            }
            if (logManager.getCommitIndex() < req.leaderCommit) logManager.setCommitIndex(req.leaderCommit);
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


    public AppendEntriesResponse handleAppendEntries(AppendEntriesRequest req){
        heartbeatTracker.updateHeartbeat();
        synchronized (raftState.getLock()){
            synchronized (logManager.lock){
                if (req.term > raftState.getCurrentTerm()) raftState.setTerm(req.term);
                else if (req.term == raftState.getCurrentTerm()) {
                    // do nothing
                    }
                else return new AppendEntriesResponse(raftState.getCurrentTerm(), false);

                if (req.leaderCommit > logManager.getCommitIndex()){
                    logManager.setCommitIndex(Math.min(req.leaderCommit, logManager.size()));
                }

                LogEntry lg = logManager.get(req.prevLogIndex);
                if (lg == null) return new AppendEntriesResponse(raftState.getCurrentTerm(), false); 

                if (lg.term != req.prevLogTerm) {
                    // truncate and then return false!!!
                    logManager.truncate(lg.index);
                    return new AppendEntriesResponse(raftState.getCurrentTerm(), false);
                }
                
                if (req.entries == null)  return new AppendEntriesResponse(raftState.getCurrentTerm(), true);



            }
        }

        return new AppendEntriesResponse(raftState.getCurrentTerm(), true);
    }
}