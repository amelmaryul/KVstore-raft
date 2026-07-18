
public class RaftState {
   private volatile int currentTerm = 0;
   private String votedFor = null;
   private String role = "Follower";
   private Object lock = this;
   FileStore fileStore;

   public RaftState(FileStore fileStore){
    this.fileStore = fileStore;
    String[] arr = fileStore.initializeRaftState();
    
    if (arr != null){
        this.currentTerm = Integer.valueOf(arr[0]);
        this.votedFor = arr[1];
    }
    else {
        this.currentTerm = 0;
        this.votedFor = null;
    }
        


  }


   public synchronized boolean becomeCandidate(String nodeId){
    if (!this.role.equals("Candidate")){
        return false;
    }
    this.currentTerm++;
    this.votedFor = nodeId;
    System.out.println("Updated term to: " + String.valueOf(this.currentTerm));
    fileStore.updateRaftState(this.currentTerm, nodeId);
    return true;
   }


   public synchronized void incrementTerm(){
    this.currentTerm++;
    System.out.println("Updated term to: " + String.valueOf(this.currentTerm));
   }

   public synchronized void setTerm(int term){
    this.currentTerm = term;
    this.votedFor = null;
    fileStore.updateRaftState(this.currentTerm, this.votedFor);
    System.out.println("Updated term to: " + String.valueOf(term));
   }
   public synchronized void setTerm(int term, String votedFor){
    this.currentTerm = term;
    this.votedFor = votedFor;
    this.role = "Follower";
    fileStore.updateRaftState(this.currentTerm, this.votedFor);
    System.out.println("Updated term to: " + String.valueOf(term));
   }

   public synchronized int getCurrentTerm(){
    return this.currentTerm;
   }

   public synchronized void setVotedFor(String node){
    this.votedFor = node;
   }

   public synchronized String getVotedFor(){
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
