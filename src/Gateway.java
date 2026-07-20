public class Gateway {
    String leaderId;
    public static final String nodeId = System.getenv("GATEWAY_ID");


    public void start(){
        
        new Thread(() -> new GatewayServerClientFacing(this).start()).start();
        new Thread(() -> new GatewayServer(this).start()).start();
    }


    public synchronized void setLeaderId(String id){
        this.leaderId = id;
    }

    public synchronized String getLeaderId(){
        return this.leaderId;
    }








    public static void main(String[] args) {
        new Gateway().start();
        
    }
}
