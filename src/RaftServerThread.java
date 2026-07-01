import java.net.ServerSocket;
import java.net.Socket;

public class RaftServerThread implements Runnable {
    RaftNode raftNode;


    public RaftServerThread(RaftNode raftNode){
        this.raftNode = raftNode;

    }
    

    public void run(){
        try{
            ServerSocket server = new ServerSocket(raftNode.nodeId);
            
            while (true){
                Socket socket = server.accept();
                Thread thread = new Thread(new RaftConnectionThread(raftNode, socket));
                thread.start();
            }




            
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
