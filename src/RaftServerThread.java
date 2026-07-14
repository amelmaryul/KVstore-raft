import java.net.ServerSocket;
import java.net.Socket;

public class RaftServerThread implements Runnable {
    RequestHandler requestHandler;
    ReplicationManager replicationManager;
    String nodeId;

    public RaftServerThread(String nodeId, RequestHandler requestHandler, ReplicationManager replicationManager){
        this.requestHandler = requestHandler;
        this.replicationManager = replicationManager;
        this.nodeId = nodeId;
    }
    

    public void run(){
        try{
            ServerSocket server = new ServerSocket(8081);
            
            while (true){
                Socket socket = server.accept();
                Thread thread = new Thread(new RaftConnectionThread(socket, requestHandler, replicationManager));
                thread.start();
            }




            
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
