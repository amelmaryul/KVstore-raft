import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaftNode {
    
    volatile int currentTerm = 0;
    int nodeId;
    final static List<Integer> ports = new ArrayList<>(Arrays.asList(5051,5052,5053, 5054, 5055));
    volatile String role = "Follower";
    volatile Integer votedFor = null;
    volatile long lastHeartBeatTime = 0;
    long electionTimeout = 150 + (long)(Math.random() * 151);
    
    // erm what are nextIndex and matchIndex lolski. no clue so i wont be adding them for now. 

    // apc stuff
    int lastApplied = 0;
    private List<LogEntry> log = new ArrayList<>();
    volatile int commited = 0;

    // leader state stuff
    Map<Integer, Integer> nextIndex = new HashMap<>();
    private Map<Integer, Integer> matchIndex = new HashMap<>();

    private final Object lock = new Object();
    final Object logLock = new Object();
    private final Object matchLock = new Object();


    StorageEngine storageEngine = StorageEngine.getInstance();


    public RaftNode(int nodeId){
        this.nodeId = nodeId;
        log.add(new LogEntry(null, 0, 0));

        // this is not thread safe
        for (int port : ports){
            updateNextIndex(port, 0); // creates keys for each node.
            updateMatchIndex(port, 0);
        }
        updateMatchIndex(nodeId, getLogSize()-1);
    }

    public void start(){
        try{
            Thread thread = new Thread(new RaftServerThread(this)); // serversocket here just sat down listening thats all
            thread.start();

            new Thread(() -> {
                /// yesss
                while (true){
                    try{

                        String[] command = storageEngine.queue.take();
                        if (role.equals("Leader")) {
                            sendLogs(command);
                        }


                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                

            }).start();


            new Thread(() -> {
                while (true){
                    
                    if (lastApplied < commited){ // what if commit gets updated mid thread now. fuck you. 
                        while (lastApplied < commited){
                            LogEntry le = readLog(++lastApplied);
                            String[] command = le.command;
                            storageEngine.set(command[1], command[2]);
                        }
                    }
                    try{
                        Thread.sleep(150);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
                
            }).start();
             

            while (true){
                try{

                    if (role.equals("Leader")) {
                        sendHeartbeats();
                        System.out.println("Heartbeats Sent!");
                        //System.out.println("[Leader] Sending heartbeats!!");
                        Thread.sleep(100);
                    }

                    else if (role.equals("Candidate")){
                        startElection();
                    } 

                    else if (role.equals("Follower")) followerBehavior();

                } catch (Exception e){
                    e.printStackTrace();
                }

            }


        } catch (Exception exception){
            exception.printStackTrace();
        }


    }


    private void followerBehavior() throws InterruptedException, IOException, ClassNotFoundException{
        Thread.sleep(50);
        if ((System.currentTimeMillis() - lastHeartBeatTime) > electionTimeout){
            setRole("Candidate");
            //System.out.println("[follower behavior] I am a candidate now!");
        }
    }


    public synchronized void updatehearbeat(long newHeartBeatTime){
        this.lastHeartBeatTime = newHeartBeatTime;
    }
    



    private void startElection() throws IOException, ClassNotFoundException{
        setTerm(currentTerm + 1);
        setVoted(nodeId);

        int votesReceived = 1;

        for (int port : ports){
            if (port == nodeId) continue;
            try{

                Socket socket = new Socket("localhost", port);
                ObjectOutputStream out_socket = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in_socket = new ObjectInputStream(socket.getInputStream());

                //socket.setSoTimeout(35);

                RequestVoteRequest req = new RequestVoteRequest(currentTerm, nodeId, 0, 0);
                out_socket.writeObject(req);
                out_socket.flush();

                RequestVoteResponse response =  (RequestVoteResponse) in_socket.readObject();
                if (response.voteGranted) votesReceived++;

                socket.close();
                
            } catch (Exception e){
                //System.out.println("Port: " + String.valueOf(port) + " is dead");
            }


        }

        if (votesReceived > ports.size() / 2){
            setRole("Leader");
            reInitializeNextIndex(getLogSize());
            System.out.println("[Leader] I won the election");
        }
        else setRole("Follower");
        updatehearbeat(System.currentTimeMillis());
    }



    private void sendHeartbeats() throws IOException, ClassNotFoundException{
        for (int port : ports){
            if (port == nodeId) continue;

            try{

                Socket socket = new Socket("localhost", port);
                //socket.setSoTimeout(150);
                ObjectOutputStream out_socket = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in_socket = new ObjectInputStream(socket.getInputStream());



                int prevLogIndex = getPrevLogIndex(port); // diagnosed i am stuck here or next line. why.
                LogEntry lg = readLog(prevLogIndex); 
                int prevLogTerm = (lg == null) ? 0 : lg.term;

                // i think somehwere here i get stuck forever 

                AppendEntriesRequest apc = new AppendEntriesRequest(currentTerm, nodeId, prevLogIndex, prevLogTerm, null, commited);

                out_socket.writeObject(apc);
                out_socket.flush();
                
                AppendEntriesResponse response = (AppendEntriesResponse) in_socket.readObject();
                if (response.term > currentTerm){
                    setTerm(response.term);
                    setRole("Follower");
                    setElectionTimeout(150 + (long) (Math.random() * 151));
                    socket.close();
                    return;
                }
                //System.out.println("[RaftNode] Heartbeat sent to: " + String.valueOf(port) );
                socket.close();



                
            } catch (Exception exception){
                //System.out.println("Port: " + String.valueOf(port) + " is dead");
            }

        }

    }




    public void sendLogs(String[] command){
        System.out.println("[Leader] I am attempting to create the Apc!!!!!!!");

        LogEntry newLog = new LogEntry(command, currentTerm, getLogSize());
        writeLog(newLog);
        updateMatchIndex(nodeId, getLogSize()-1);

        for (int port : ports){
            if (port == nodeId) continue;

            new Thread(() -> {
                boolean success = false;


                while (!success){

                    try{

                        Socket socket = new Socket("localhost", port);
                        ObjectOutputStream out_socket = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream in_socket = new ObjectInputStream(socket.getInputStream());


                        int prevLogIndex = getPrevLogIndex(port);
                        int prevLogTerm = readLog(prevLogIndex).term;
                        int lastIndex = getLogSize();
                        List<LogEntry> entries = new ArrayList<>();
                        addLogstoEntries(prevLogIndex +1, lastIndex, entries); // this should be thread safe

                        AppendEntriesRequest apc = new AppendEntriesRequest(currentTerm, nodeId, prevLogIndex, prevLogTerm, entries, commited);


                        out_socket.writeObject(apc);
                        out_socket.flush();
                        System.out.println("Command Sent to port: " + String.valueOf(port));
                        
                        AppendEntriesResponse response = (AppendEntriesResponse) in_socket.readObject(); 
                        success = response.success;


                        if (!response.success){
                            updatePrevLogIndex(port, prevLogIndex-1);
                        }

                        else if (response.success){
                            updateMatchIndex(port, lastIndex-1);
                            updateNextIndex(port, lastIndex); // this is thread safe. 
                            updateCommit();




                        }

                        socket.close();

                    } catch (Exception e){
                        if (e instanceof ConnectException){
                            success = true;
                        }
                        e.printStackTrace();
                    }
                    }

            }).start();


        }
    }


    public synchronized void setRole(String role){
        this.role = role;
    }

    public synchronized void setVoted(Integer voteId){
        this.votedFor = voteId;
    }
    
    public synchronized void setTerm(int term){
        this.currentTerm = term;
    }


    public synchronized void setElectionTimeout(long time){
        this.electionTimeout = time;
    }


    public LogEntry readLog(int index){
        synchronized (logLock){
            if (index > log.size()-1 ){
                return null;
            }
            return log.get(index);
        }

    }

    public int getLogSize(){
        synchronized (logLock){
            return log.size();
        }
    }


    public void writeLog(LogEntry logEntry){
        synchronized (logLock){
            log.add(logEntry);
        } 
    }


    public synchronized void updateCommitIndex(int index){
        this.commited = index;
    }

    public void reInitializeNextIndex(int index){ 
        synchronized (lock){
            for (int port: ports){
                nextIndex.put(port, index);
            }
        }
    }

    public void updateNextIndex(int port, int index){
        synchronized (lock){
            nextIndex.put(port, index);
        }
    }

    public int getPrevLogIndex(int port){
        synchronized (lock) {
            return nextIndex.get(port);
        }
    }

    public void updatePrevLogIndex(int port, int index){
        synchronized (lock){
            nextIndex.put(port, index);
        }
    }



    public void addLogstoEntries(int start, int end, List<LogEntry> entries){
        synchronized (logLock) {
            for (int i = start; i < end; i++){
                entries.add(readLog(i));
            }
        }
    }

    public void updateCommit(){
        synchronized (matchLock){
            // check if commited can be updated. i believe only this code here should update commit? we'll see. 
            // if this block can only touch commit we dont have to make commit like lock secured completely bcs i mean this is lock secured.

            int n = ports.size();
            int[] arr = new int[n];

            for (int i = 0; i < n; i++){
                arr[i] = matchIndex.get(ports.get(i));
            }
            Arrays.sort(arr);

            int mid = n / 2;


            updateCommitIndex(arr[mid]);


        }
    }


    public void updateMatchIndex(int port, int commit){
        synchronized (matchLock){
            matchIndex.put(port, commit);
        }
    }


    public int getMatchIndex(int port){
        synchronized (matchLock){
            return matchIndex.get(port);
        }
    }



    public static void main(String[] args){
        RaftNode node = new RaftNode(Integer.valueOf(args[1]));
        node.start();
    }
}
