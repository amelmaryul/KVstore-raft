import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class RaftConnectionThread implements Runnable {
    Socket socket;
    RequestHandler requestHandler;
    ReplicationManager replicationManager;
    

    public RaftConnectionThread(Socket socket, RequestHandler requestHandler, ReplicationManager replicationManager){
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.replicationManager = replicationManager;
    }


    public void run(){
        try{
            ObjectOutputStream out_socket = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in_socket = new ObjectInputStream(socket.getInputStream());

            Message msg = (Message) in_socket.readObject();
            if (msg instanceof RequestVoteRequest){
                RequestVoteRequest req = (RequestVoteRequest) msg;
                RequestVoteResponse response = requestHandler.handleRequestVote(req);


                out_socket.writeObject(response);
                out_socket.flush();

            }

////////////////////////////// handle apc here ////////////////////////////////////////////////////////
            else if (msg instanceof AppendEntriesRequest){
                AppendEntriesRequest req = (AppendEntriesRequest) msg;
                AppendEntriesResponse response = (AppendEntriesResponse) requestHandler.handleAppendEntries(req);
                boolean replicated = false;

                if (response.success){
                    replicationManager.replicate(req.entries);
                    replicated = true;
                }
                out_socket.writeObject(response);
                
                

                while (!replicated){
                msg = (AppendEntriesRequest) in_socket.readObject();
                req = (AppendEntriesRequest) msg;
                response = (AppendEntriesResponse) requestHandler.handleAppendEntries(req);
                
                if (response.success){
                    replicationManager.replicate(req.entries);
                    replicated = true;
                }
                out_socket.writeObject(response);
                    
                }

            }
            socket.close();

        } catch (Exception exception){
            exception.printStackTrace();
        }


    }


    
}
