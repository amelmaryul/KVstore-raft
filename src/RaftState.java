
public class RaftState {
   private volatile int currentTerm = 0;
   private Integer votedFor = null;
   private String role = "Follower";
   private Object lock = this;
   FileStore fileStore;

   public RaftState(FileStore fileStore){
    this.fileStore = fileStore;
    int[] arr = fileStore.initializeRaftState();
    if (arr != null){
        this.currentTerm = arr[0];
        this.votedFor = arr[1];
    }
    else {
        this.currentTerm = 0;
        this.votedFor = null;
    }
  }


   public synchronized boolean becomeCandidate(Integer nodeId){
    if (!this.role.equals("Candidate")){
        return false;
    }
    this.currentTerm++;
    this.votedFor = nodeId;
    fileStore.updateRaftState(this.currentTerm, nodeId);
    return true;
   }


   public synchronized void incrementTerm(){
    this.currentTerm++;
   }

   public synchronized void setTerm(int term){
    this.currentTerm = term;
    this.votedFor = null;
    fileStore.updateRaftState(this.currentTerm, this.votedFor);
   }
   public synchronized void setTerm(int term, Integer votedFor){
    this.currentTerm = term;
    this.votedFor = votedFor;
    fileStore.updateRaftState(this.currentTerm, this.votedFor);
   }

   public synchronized int getCurrentTerm(){
    return this.currentTerm;
   }

   public synchronized void setVotedFor(Integer node){
    this.votedFor = node;
   }

   public synchronized Integer getVotedFor(){
    return this.votedFor;
   }

   public synchronized String getRole(){
    return this.role;
   }

   public synchronized void setRole(String role){
    this.role = role;
   }

   public synchronized Object getLock(){
    return this.lock;
   }


}
