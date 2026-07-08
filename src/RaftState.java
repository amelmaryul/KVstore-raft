public class RaftState {
   private volatile int currentTerm = 0;
   private Integer votedFor = null;
   private String role = "Follower";
   private Object lock = new Object();


   public synchronized boolean becomeCandidate(Integer nodeId){
    if (!this.role.equals("Candidate")){
        return false;
    }
    this.currentTerm++;
    this.votedFor = nodeId;
    return true;
   }


   public synchronized void incrementTerm(){
    this.currentTerm++;
   }

   public synchronized void setTerm(int term){
    this.currentTerm = term;
    this.votedFor = null;
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
