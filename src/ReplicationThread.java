import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicationThread implements Runnable{
    List<Socket> sockets;
    List<Integer> ports;
    StorageEngine storageEngine = StorageEngine.getInstance();
    int leader;
    int myport;

    public ReplicationThread(List<Socket> sockets, List<Integer> ports, int leader, int myport){
        this.sockets = sockets;
        this.ports = ports;
        this.leader = leader;
        this.myport = myport;
    }

    public void run(){
        try{
            /*
            this tries to connect to all other nodes
            after it connects to all nodes it holds each socket reference in the list sockets
            */

                //BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //PrintWriter out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (sockets.size() < ports.size()){
                try{
                    for (int p : ports){
                        if (true){
                            Socket socket = new Socket("localhost", p);
                            //map.put(p,socket);
                            sockets.add(socket);
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }



            System.out.println("[ReplicationThread] i can confirm you have started this thread");

           
                while (true){
                String[] messageArray = storageEngine.queue.take();
                String message = Parsing.buildRespString(messageArray);
                for (Socket socket : sockets){
                    if (socket.getPort() == socket.getLocalPort()) continue; // doenst work baka. 
                    PrintWriter out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    out_socket.println(message);
                    System.out.println("[ReplicationThread] message sent to node at port: " + String.valueOf(socket.getPort()));
                }

            }
            

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}