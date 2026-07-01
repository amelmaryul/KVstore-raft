import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class RaftConnectionThread implements Runnable {
    RaftNode raftNode;
    Socket socket;
    

    public RaftConnectionThread(RaftNode raftNode, Socket socket){
        this.raftNode = raftNode;
        this.socket = socket;
    }


    public void run(){
        try{
            ObjectOutputStream out_socket = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in_socket = new ObjectInputStream(socket.getInputStream());


            // now i might get either an append or a requestvote message. i need to handle both of them!
            Message msg = (Message) in_socket.readObject();
            if (msg instanceof RequestVoteRequest){
                RequestVoteRequest req = (RequestVoteRequest) msg;
                boolean voteGranted = false;

                if (req.term >= raftNode.currentTerm){
                    if (req.term > raftNode.currentTerm){
                        
                        voteGranted = true;
                        raftNode.setTerm(req.term);
                        raftNode.setVoted(req.candidateId);
                    }
                    else if (req.term == raftNode.currentTerm && raftNode.votedFor == null){ 

                        voteGranted = true;
                        raftNode.setVoted(req.candidateId);
                    }


                }

                out_socket.writeObject(new RequestVoteResponse(req.term, voteGranted));
                out_socket.flush();

            }

////////////////////////////// handle apc here ////////////////////////////////////////////////////////
            else if (msg instanceof AppendEntriesRequest){
                AppendEntriesRequest req = (AppendEntriesRequest) msg;
                if (req.entries == null){

                    if (req.term >= raftNode.currentTerm){
                        if (req.term > raftNode.currentTerm){
                            raftNode.setTerm(req.term);
                            raftNode.setVoted(null);
                        }

                        raftNode.updatehearbeat(System.currentTimeMillis());
                        if (!raftNode.role.equals("Follower")){
                            raftNode.setRole("Follower");
                            raftNode.updatehearbeat(System.currentTimeMillis());
                        }

                        out_socket.writeObject(new AppendEntriesResponse(req.term, true));
                        out_socket.flush();
                        //System.out.println("[RaftConnectionThread] Heartbeat received from: " + String.valueOf(req.leaderId));

                        // update rsm if its behind commint index
                        if (req.leaderCommit > raftNode.commited){
                            int newCommit = Math.min(req.leaderCommit, raftNode.getLogSize()-1);
                            raftNode.updateCommitIndex(newCommit);
                        }



                    }
                    else{
                        out_socket.writeObject(new AppendEntriesResponse(req.term, false));
                        out_socket.flush();
                    }
                }

                else {
                    LogEntry logEntry = raftNode.readLog(req.prevLogIndex);
                    if (logEntry != null && logEntry.term == req.prevLogTerm){
                        synchronized (raftNode.logLock){
                            for (LogEntry lg : req.entries){
                                raftNode.writeLog(lg);
                            }
                    }


                        out_socket.writeObject(new AppendEntriesResponse(raftNode.currentTerm, true));
                        out_socket.flush();
                        System.out.println("[Follower] yes i recieved replication and it was successful");


                    }

                    else {
                        out_socket.writeObject(new AppendEntriesResponse(raftNode.currentTerm, false));
                        out_socket.flush();
                        System.out.println("[Follower] yes i recieved replication and it was NOT successful");

                    }



                    // update rsm if its behind commint index
                    if (req.leaderCommit > raftNode.commited){
                        int newCommit = Math.min(req.leaderCommit, raftNode.getLogSize()-1);
                        raftNode.updateCommitIndex(newCommit);
                    }

                }
            }

            socket.close();


        } catch (Exception exception){
            exception.printStackTrace();
        }


    }


    
}
