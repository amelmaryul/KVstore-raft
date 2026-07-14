public class HeartbeatTracker {

    volatile long lastHeartbeat = 0;
    long electionTimeout = 1500 + (long)(Math.random() * 2001); 
    
    


    public synchronized void updateHeartbeat(){
        this.lastHeartbeat = System.currentTimeMillis();
    } 

    public synchronized void updateHeartbeat(long newTime){
        this.lastHeartbeat = newTime;
    }

    public synchronized void updateElectionTimeout(){
        this.electionTimeout = 1500 + (long)(Math.random() * 2001);
    }

    public synchronized void updateElectionTimeout(long newElectionTimeout){
        this.electionTimeout = newElectionTimeout;
    }

    public synchronized boolean isExpired(){
        if (System.currentTimeMillis() - lastHeartbeat > electionTimeout){
            return true;
        }
        return false;
    }

    
}
